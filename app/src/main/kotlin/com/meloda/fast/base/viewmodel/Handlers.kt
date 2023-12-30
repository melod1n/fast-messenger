package com.meloda.fast.base.viewmodel

fun interface ErrorHandler {

    /**
     * @return true if error has been handled manually
     */
    suspend fun handleError(error: Throwable): Boolean
}

fun interface ResponseHandler<T> {

    suspend fun handleResponse(response: T)
}
