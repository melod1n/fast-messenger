package com.meloda.vksdk

interface OnResponseListener<T> {

    fun onResponse(response: T)

    fun onError(t: Throwable)

}