package com.meloda.mvp

interface MvpOnResponseListener<T> {

    fun onResponse(response: T)

    fun onError(t: Throwable)

}