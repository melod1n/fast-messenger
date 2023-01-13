@file:Suppress("UNCHECKED_CAST")

package com.meloda.fast.api.network

import com.google.gson.Gson
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.base.ApiResponse
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class ResultCallFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        val rawReturnType: Class<*> = getRawType(returnType)
        if (rawReturnType == Call::class.java) {
            if (returnType is ParameterizedType) {
                val callInnerType: Type = getParameterUpperBound(0, returnType)
                if (getRawType(callInnerType) == ApiAnswer::class.java) {
                    if (callInnerType is ParameterizedType) {
                        val resultInnerType = getParameterUpperBound(0, callInnerType)
                        return ResultCallAdapter<Any?>(resultInnerType)
                    }
                    return ResultCallAdapter<Nothing>(Nothing::class.java)
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

private class ResultCallAdapter<R>(private val type: Type) : CallAdapter<R, Call<ApiAnswer<R>>> {

    override fun responseType() = type

    override fun adapt(call: Call<R>): Call<ApiAnswer<R>> = ResultCall(call)
}

internal class ResultCall<T>(proxy: Call<T>) : CallDelegate<T, ApiAnswer<T>>(proxy) {

    override fun enqueueImpl(callback: Callback<ApiAnswer<T>>) {
        proxy.enqueue(ResultCallback(this, callback))
    }

    override fun cloneImpl(): ResultCall<T> {
        return ResultCall(proxy.clone())
    }

    private class ResultCallback<T>(
        private val proxy: ResultCall<T>,
        private val callback: Callback<ApiAnswer<T>>,
    ) : Callback<T> {

        val gson = Gson()

        override fun onResponse(call: Call<T>, response: Response<T>) {
            val result: ApiAnswer<T> =
                if (response.isSuccessful) {
                    val baseBody = response.body()
                    if (baseBody !is ApiResponse<*>) {
                        ApiAnswer.Success(baseBody as T)
                    } else {
                        val body = baseBody as? ApiResponse<*>
                        if (body?.error != null) {
                            VkUtils.getApiError(gson, gson.toJson(body.error))
                        } else {
                            ApiAnswer.Success(body as T)
                        }
                    }
                } else {
                    val errorBodyString = response.errorBody()?.string()

                    VkUtils.getApiError(gson, errorBodyString)
                }

            if (checkErrors(call, result)) {
                return
            }

            callback.onResponse(proxy, Response.success(result))
        }

        override fun onFailure(call: Call<T>, error: Throwable) {
            callback.onResponse(
                proxy,
                Response.success(ApiAnswer.Error(ApiError(throwable = error)))
            )
        }

        private fun checkErrors(call: Call<T>, result: ApiAnswer<*>): Boolean {
            if (result.isError()) {
                result.error.throwable?.run {
                    onFailure(call, this)
                    return true
                }
            }

            return false
        }
    }

    override fun timeout(): Timeout {
        return proxy.timeout()
    }
}

sealed class ApiAnswer<out R> {

    data class Success<out T>(val data: T) : ApiAnswer<T>()
    data class Error(val error: ApiError) : ApiAnswer<Nothing>()

    @OptIn(ExperimentalContracts::class)
    fun isSuccessful(): Boolean {
        contract {
            returns(true) implies (this@ApiAnswer is Success)
        }
        return this is Success
    }

    @OptIn(ExperimentalContracts::class)
    fun isError(): Boolean {
        contract {
            returns(true) implies (this@ApiAnswer is Error)
        }
        return this is Error
    }
}
