package com.meloda.fast.api.loader

abstract class Loader<T> {

    abstract suspend fun load(params: MutableMap<String, Any>): List<T>
    abstract suspend fun loadSingle(params: MutableMap<String, Any>): T

}