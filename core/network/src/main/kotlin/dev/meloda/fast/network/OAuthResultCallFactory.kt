package dev.meloda.fast.network

import dev.meloda.fast.model.api.responses.AuthDirectErrorOnlyResponse
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
                        return ResultCallAdapter<Any>(resultInnerType, moshi)
                    }

                    return ResultCallAdapter<Nothing>(Nothing::class.java, moshi)
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

private class ResultCallAdapter<R : Any>(
    private val type: Type,
    private val moshi: Moshi
) : CallAdapter<R, Call<OAuthResponse<R, AuthDirectErrorOnlyResponse>>> {

    override fun responseType() = type

    override fun adapt(call: Call<R>): Call<OAuthResponse<R, AuthDirectErrorOnlyResponse>> =
        ResultCall(call, moshi)
}

internal class ResultCall<R : Any>(
    proxy: Call<R>,
    private val moshi: Moshi
) : CallDelegate<R, OAuthResponse<R, AuthDirectErrorOnlyResponse>>(proxy) {

    override fun enqueueImpl(callback: Callback<OAuthResponse<R, AuthDirectErrorOnlyResponse>>) {
        proxy.enqueue(ResultCallback(this, callback, moshi))
    }

    override fun cloneImpl(): ResultCall<R> {
        return ResultCall(proxy.clone(), moshi)
    }

    private class ResultCallback<R : Any>(
        private val proxy: ResultCall<R>,
        private val callback: Callback<OAuthResponse<R, AuthDirectErrorOnlyResponse>>,
        private val moshi: Moshi
    ) : Callback<R> {

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

                    val baseError = moshi.adapter(AuthDirectErrorOnlyResponse::class.java)
                        .fromJson(errorBodyString.orEmpty()) ?: return

                    callback.onResponse(
                        proxy,
                        Response.success(OAuthResponse.Error(baseError) as OAuthResponse<R, AuthDirectErrorOnlyResponse>)
                    )
                }
            }
        }

        override fun onFailure(call: Call<R>, error: Throwable) {
            throw error
        }
    }

    override fun timeout(): Timeout {
        return proxy.timeout()
    }
}
