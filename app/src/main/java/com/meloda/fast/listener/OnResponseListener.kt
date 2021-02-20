package com.meloda.fast.listener

interface OnResponseListener<T> {

    fun onResponse(response: T)

    fun onError(t: Throwable)

}