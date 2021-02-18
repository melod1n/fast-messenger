package ru.melod1n.project.vkm.listener

interface OnResponseListener<T> {

    fun onResponse(response: T)

    fun onError(t: Throwable)

}