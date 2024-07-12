package com.meloda.app.fast.network

import android.util.Log
import com.squareup.moshi.Moshi
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class OAuthResultCallFactory(private val moshi: Moshi) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        val rawReturnType: Class<*> = getRawType(returnType)

        if (rawReturnType == Call::class.java) {
            if (returnType is ParameterizedType) {
                val callInnerType: Type = getParameterUpperBound(0, returnType)

                if (getRawType(callInnerType) == OAuthResponse::class.java) {
                    if (callInnerType is ParameterizedType) {
                        val resultInnerType = getParameterUpperBound(0, callInnerType)
                        return ResultCallAdapter<Any, OAuthError>(resultInnerType, moshi)
                    }

                    return ResultCallAdapter<Nothing, Nothing>(Nothing::class.java, moshi)
                }
            }
        }
        return null
    }
}

internal abstract class CallDelegate<In, Out>(protected val proxy: Call<In>) : Call<Out> {

    override fun execute(): Response<Out> = throw NotImplementedError()

    final override fun enqueue(callback: Callback<Out>) = enqueueImpl(callback)

    final override fun clone(): Call<Out> = cloneImpl()

    override fun cancel() = proxy.cancel()

    override fun request(): Request = proxy.request()

    override fun isExecuted() = proxy.isExecuted

    override fun isCanceled() = proxy.isCanceled

    abstract fun enqueueImpl(callback: Callback<Out>)

    abstract fun cloneImpl(): Call<Out>
}

private class ResultCallAdapter<R : Any, E : OAuthError>(
    private val type: Type,
    private val moshi: Moshi
) : CallAdapter<R, Call<OAuthResponse<R, E>>> {

    override fun responseType() = type

    override fun adapt(call: Call<R>): Call<OAuthResponse<R, E>> = ResultCall(call, moshi)
}

internal class ResultCall<R : Any, E : OAuthError>(
    proxy: Call<R>,
    private val moshi: Moshi
) : CallDelegate<R, OAuthResponse<R, E>>(proxy) {

    override fun enqueueImpl(callback: Callback<OAuthResponse<R, E>>) {
        proxy.enqueue(ResultCallback(this, callback, moshi))
    }

    override fun cloneImpl(): ResultCall<R, E> {
        return ResultCall(proxy.clone(), moshi)
    }

    private class ResultCallback<R : Any, E : OAuthError>(
        private val proxy: ResultCall<R, E>,
        private val callback: Callback<OAuthResponse<R, E>>,
        private val moshi: Moshi
    ) : Callback<R> {

        @Suppress("UNCHECKED_CAST")
        override fun onResponse(call: Call<R>, response: Response<R>) {
            when {
                response.isSuccessful -> {
                    val baseBody = response.body()

                    baseBody?.let {
                        callback.onResponse(
                            proxy,
                            Response.success(OAuthResponse.Success(baseBody))
                        )
                    }
                }

                else -> {
                    val errorBodyString = response.errorBody()?.string()

                    val baseError: OAuthError = moshi.adapter(OAuthError::class.java)
                        .fromJson(errorBodyString.orEmpty()) ?: return

                    val error: OAuthError? = when (baseError.error) {
                        "9;Flood control" -> {
                            moshi.adapter(TooManyTriesError::class.java)
                                .fromJson(errorBodyString.orEmpty())
                        }

                        "invalid_client" -> {
                            moshi.adapter(InvalidCredentialsError::class.java)
                                .fromJson(errorBodyString.orEmpty())
                        }

                        "need_captcha" -> {
                            moshi.adapter(CaptchaRequiredError::class.java)
                                .fromJson(errorBodyString.orEmpty())
                        }

                        "invalid_request" -> {
                            when (val type = baseError.errorType) {
                                "wrong_otp" -> {
                                    moshi.adapter(WrongTwoFaCodeError::class.java)
                                        .fromJson(errorBodyString.orEmpty())
                                }

                                "otp_format_is_incorrect" -> {
                                    moshi.adapter(WrongTwoFaCodeFormatError::class.java)
                                        .fromJson(errorBodyString.orEmpty())
                                }

                                else -> {
                                    Log.d(
                                        "ResultCallback",
                                        "onResponse: invalid_request; error_type: $type"
                                    )

                                    error("Unknown type: $type")
                                }
                            }
                        }

                        "need_validation" -> {
                            when (val description = baseError.errorDescription) {
                                "user has been banned" -> {
                                    moshi.adapter(UserBannedError::class.java)
                                        .fromJson(errorBodyString.orEmpty())
                                }

                                "sms sent, use code param",
                                "use app code" -> {
                                    moshi.adapter(ValidationRequiredError::class.java)
                                        .fromJson(errorBodyString.orEmpty())
                                }

                                else -> {
                                    Log.d(
                                        "ResultCallback",
                                        "onResponse: need_validation; description: $description"
                                    )

                                    error("Unknown description: $description")
                                }
                            }
                        }

                        else -> null
                    }

                    error?.let {
                        callback.onResponse(
                            proxy,
                            Response.success(OAuthResponse.Error(error) as OAuthResponse<R, E>)
                        )
                    }
                }
            }
        }

        override fun onFailure(call: Call<R>, error: Throwable) {
            val b = error
            // TODO: 12/04/2024, Danil Nikolaev: handle
//            callback.onResponse(
//                proxy,
//                Response.success(OAuthAnswer.Error((throwable = error)))
//            )
        }
    }

    override fun timeout(): Timeout {
        return proxy.timeout()
    }
}
