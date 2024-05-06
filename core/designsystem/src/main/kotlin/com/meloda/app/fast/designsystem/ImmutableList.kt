package com.meloda.app.fast.designsystem

import androidx.compose.runtime.Immutable

@Immutable
class ImmutableList<T>(val values: List<T>) {

    operator fun get(index: Int): T? {
        return values[index]
    }

    inline fun forEach(action: (T) -> Unit) {
        for (element in values) action(element)
    }

    fun toMutableList(): MutableList<T> = values.toMutableList()

    companion object {
        fun <T> copyOf(collection: Collection<T>): ImmutableList<T> =
            ImmutableList(collection.toList())
    }
}
