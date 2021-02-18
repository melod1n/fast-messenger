package ru.melod1n.project.vkm.base.mvp

interface MvpOnLoadListener<T> {

    fun onResponse(response: T)

    fun onError(t: Throwable)

}