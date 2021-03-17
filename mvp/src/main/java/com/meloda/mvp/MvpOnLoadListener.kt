package com.meloda.mvp

interface MvpOnLoadListener {

    fun onSuccess()

    fun onError(t: Throwable)

}