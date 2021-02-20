package com.meloda.fast.extensions

object ArrayExtensions {

    fun ByteArray?.isNullOrEmpty() = this == null || this.isEmpty()

    fun <E> List<E>.asArrayList(): ArrayList<E> {
        return ArrayList(this)
    }

}