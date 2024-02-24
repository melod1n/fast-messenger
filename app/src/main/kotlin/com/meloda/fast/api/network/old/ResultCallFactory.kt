package com.meloda.fast.api.network.old

//class ResultCallFactory(private val moshi: Moshi) : CallAdapter.Factory() {
//    override fun get(
//        returnType: Type,
//        annotations: Array<out Annotation>,
//        retrofit: Retrofit,
//    ): CallAdapter<*, *>? {
//        val rawReturnType: Class<*> = getRawType(returnType)
//        if (rawReturnType == Call::class.java) {
//            if (returnType is ParameterizedType) {
//                val callInnerType: Type = getParameterUpperBound(0, returnType)
//                if (getRawType(callInnerType) == ApiAnswer::class.java) {
//                    if (callInnerType is ParameterizedType) {
//                        val resultInnerType = getParameterUpperBound(0, callInnerType)
//                        return ResultCallAdapter<Any?>(resultInnerType, moshi)
//                    }
//                    return ResultCallAdapter<Nothing>(Nothing::class.java, moshi)
//                }
//            }
//        }
//        return null
//    }
//}

//internal abstract class CallDelegate<In, Out>(protected val proxy: Call<In>) : Call<Out> {
//
//    override fun execute(): Response<Out> = throw NotImplementedError()
//
//    final override fun enqueue(callback: Callback<Out>) = enqueueImpl(callback)
//
//    final override fun clone(): Call<Out> = cloneImpl()
//
//    override fun cancel() = proxy.cancel()
//
//    override fun request(): Request = proxy.request()
//
//    override fun isExecuted() = proxy.isExecuted
//
//    override fun isCanceled() = proxy.isCanceled
//
//    abstract fun enqueueImpl(callback: Callback<Out>)
//
//    abstract fun cloneImpl(): Call<Out>
//}
//
//private class ResultCallAdapter<R>(private val type: Type, private val moshi: Moshi) :
//    CallAdapter<R, Call<ApiAnswer<R>>> {
//
//    override fun responseType() = type
//
//    override fun adapt(call: Call<R>): Call<ApiAnswer<R>> = ResultCall(call, moshi)
//}

//internal class ResultCall<T>(
//    proxy: Call<T>,
//    private val moshi: Moshi
//) : CallDelegate<T, ApiAnswer<T>>(proxy) {
//
//    override fun enqueueImpl(callback: Callback<ApiAnswer<T>>) {
//        proxy.enqueue(ResultCallback(this, callback, moshi))
//    }
//
//    override fun cloneImpl(): ResultCall<T> {
//        return ResultCall(proxy.clone(), moshi)
//    }
//
//    private class ResultCallback<T>(
//        private val proxy: ResultCall<T>,
//        private val callback: Callback<ApiAnswer<T>>,
//        private val moshi: Moshi
//    ) : Callback<T> {
//
//        override fun onResponse(call: Call<T>, response: Response<T>) {
//            val result: ApiAnswer<T> =
//                if (response.isSuccessful) {
//                    val baseBody = response.body()
//                    if (baseBody !is ApiResponse<*>) {
//                        ApiAnswer.Success(baseBody as T)
//                    } else {
//                        val body = baseBody as? ApiResponse<*>
//                        ApiAnswer.Success(body as T)
//                        if (body?.error != null) {
//                            VkUtils.getApiError(moshi, moshi.toJson(body.error))
//                        } else {
//                            ApiAnswer.Success(body as T)
//                        }
//                    }
//                } else {
//                    val errorBodyString = response.errorBody()?.string()
//                    VkUtils.getError(moshi, errorBodyString)
//                    ApiAnswer.Error(ApiError())
//                }

//            if (checkErrors(call, result)) {
//                return
//            }
//
//            callback.onResponse(proxy, Response.success(result))
//        }

//        override fun onFailure(call: Call<T>, error: Throwable) {
//            callback.onResponse(
//                proxy,
//                Response.success(ApiAnswer.Error(ApiError(throwable = error)))
//            )
//        }

//        private fun checkErrors(call: Call<T>, result: ApiAnswer<*>): Boolean {
//            if (result is ApiAnswer.Error) {
//                result.error.throwable?.run {
//                    onFailure(call, this)
//                    return true
//                }
//            }
//
//            return false
//        }
//    }

//    override fun timeout(): Timeout {
//        return proxy.timeout()
//    }
//}
