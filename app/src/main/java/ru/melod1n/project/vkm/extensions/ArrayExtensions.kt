package ru.melod1n.project.vkm.extensions

import android.content.Context

object ArrayExtensions {

    fun ByteArray?.isNullOrEmpty() = this == null || this.isEmpty()

    fun <E> List<E>.asArrayList(): ArrayList<E> {
        return ArrayList(this)
    }

}