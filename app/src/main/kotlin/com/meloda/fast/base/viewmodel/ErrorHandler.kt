package com.meloda.fast.base.viewmodel

fun interface ErrorHandler {

    /**
     * @return true if error has been handled manually
     */
    suspend fun handleError(error: Throwable): Boolean
}
