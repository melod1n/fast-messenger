package dev.meloda.fast.ui.util

import androidx.compose.runtime.Immutable

@Immutable
class ImmutableList<T>(val values: List<T>) : Iterable<T> {

    constructor(size: Int, init: (index: Int) -> T) : this(MutableList(size, init))

    operator fun get(index: Int): T = values[index]

    inline fun forEach(action: (T) -> Unit) {
        for (element in values) action(element)
    }

    inline fun <R> map(transform: (T) -> R): ImmutableList<R> {
        return values.map(transform).toImmutableList()
    }

    inline fun <R> mapNotNull(transform: (T) -> R?): ImmutableList<R> {
        return values.mapNotNull(transform).toImmutableList()
    }

    inline fun <R> mapIndexed(transform: (index: Int, T) -> R): ImmutableList<R> {
        return values.mapIndexed(transform).toImmutableList()
    }

    fun singleOrNull(): T? {
        return if (values.size == 1) this[0] else null
    }

    fun isEmpty(): Boolean = values.isEmpty()

    fun isNotEmpty(): Boolean = !isEmpty()

    inline fun singleOrNull(predicate: (T) -> Boolean): T? {
        var single: T? = null
        var found = false
        for (element in this) {
            if (predicate(element)) {
                if (found) return null
                single = element
                found = true
            }
        }
        if (!found) return null
        return single
    }

    val size: Int get() = values.size

    companion object {
        fun <T> copyOf(collection: Collection<T>): ImmutableList<T> =
            ImmutableList(collection.toList())

        fun <T> List<T>.toImmutableList(): ImmutableList<T> = ImmutableList(this)

        fun <T> empty(): ImmutableList<T> = ImmutableList(emptyList())

        fun <T> of(vararg elements: T) =
            if (elements.isNotEmpty()) copyOf(elements.asList()) else empty()

        fun <T> of(element: T) = ImmutableList(listOf(element))
    }

    override fun iterator(): Iterator<T> = values.listIterator()
}
