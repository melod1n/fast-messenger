package ru.melod1n.project.vkm.util

import ru.melod1n.project.vkm.extensions.ArrayExtensions.asArrayList
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
//        if (arrayList.isEmpty()) return ""
//
//        val builder = StringBuilder(arrayList.size * 12)
//        builder.append(arrayList[0])
//        for (i in 1 until arrayList.size) {
//            builder.append(',')
//            builder.append(arrayList[i])
//        }
//        return builder.toString()

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

}