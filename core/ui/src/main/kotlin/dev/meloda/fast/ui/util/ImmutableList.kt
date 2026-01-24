package dev.meloda.fast.ui.util

import androidx.compose.runtime.Immutable

@Immutable
class ImmutableList<T>(val values: List<T>) : Collection<T> {

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

    override fun isEmpty(): Boolean = values.isEmpty()

    override val size: Int get() = values.size

    override fun containsAll(elements: Collection<T>): Boolean {
        return values.containsAll(elements)
    }

    override fun contains(element: T): Boolean {
        return values.contains(element)
    }

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

    val lastIndex: Int get() = this.size - 1
}

fun <T> emptyImmutableList(): ImmutableList<T> = ImmutableList(emptyList())

fun <T> immutableListOf(vararg elements: T) = ImmutableList(listOf(elements = elements))

fun <T> ImmutableList<T>?.orEmpty(): ImmutableList<T> = this ?: emptyImmutableList()
