package dev.meloda.fast.common.provider

interface Provider<T> {
    fun provide(): T?
}
