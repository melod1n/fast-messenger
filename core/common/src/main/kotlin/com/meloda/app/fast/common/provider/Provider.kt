package com.meloda.app.fast.common.provider

interface Provider<T> {
    fun provide(): T?
}
