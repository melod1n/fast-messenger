package com.meloda.arrayutils

import java.util.stream.Collectors

object ArrayUtils {

    @SafeVarargs
    fun <T> asString(vararg array: T): String {
        if (array.isEmpty()) {
            return ""
        }

        val builder = StringBuilder(array.size * 12)
        builder.append(array[0])
        for (i in 1 until array.size) {
            builder.append(',')
            builder.append(array[i])
        }
        return builder.toString()
    }

    fun asString(array: IntArray): String {
        if (array.isEmpty()) {
            return ""
        }

        val builder = StringBuilder(array.size * 12)
        builder.append(array[0])
        for (i in 1 until array.size) {
            builder.append(',')
            builder.append(array[i])
        }
        return builder.toString()
    }

    fun <T> asString(arrayList: ArrayList<T>): String {
        return ArrayList<String>().apply {
            arrayList.forEach { add(it.toString()) }
        }.stream().collect(Collectors.joining(","))
    }

    fun <T> asString(list: List<T>): String = asString(list.asArrayList())

    fun <T> cut(arrayList: ArrayList<T>, offset: Int, count: Int): ArrayList<T> {
        if (arrayList.isEmpty()) return arrayListOf()

        var lastPosition = offset + count
        if (lastPosition > arrayList.size) lastPosition = arrayList.size

        return ArrayList(arrayList.subList(offset, lastPosition))
    }

    fun ByteArray?.isNullOrEmpty() = this == null || this.isEmpty()

    fun <E> List<E>.asArrayList(): ArrayList<E> {
        return ArrayList(this)
    }

}