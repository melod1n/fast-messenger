package com.meloda.fast.api

interface OnResponseListener<T> {

    fun onResponse(response: T)

    fun onError(t: Throwable)

}