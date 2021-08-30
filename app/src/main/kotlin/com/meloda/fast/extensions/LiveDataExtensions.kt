package com.meloda.fast.extensions

import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData

object LiveDataExtensions {

    operator fun <T> MutableLiveData<MutableList<T>>.set(position: Int, v: T) {
        val value = (this.value ?: arrayListOf()).apply { this[position] = v }
        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.get(position: Int): T {
        return (value as MutableList<T>)[position]
    }

    @JvmOverloads
    fun <T> MutableLiveData<MutableList<T>>.add(v: T, position: Int = -1) {
        val value = (this.value ?: arrayListOf()).apply {
            if (position == -1) this.add(v) else this.add(position, v)
        }

        this.value = value
    }

    @JvmOverloads
    fun <T> MutableLiveData<MutableList<T>>.addAll(values: List<T>, position: Int = -1) {
        val value = (this.value ?: arrayListOf()).apply {
            if (position == -1) this.addAll(values)
            else this.addAll(position, values)
        }

        this.value = value
    }

    @Suppress("TYPE_INFERENCE_ONLY_INPUT_TYPES_WARNING")
    fun <T> MutableLiveData<MutableList<T>>.removeAll(values: List<T>) {
        val value = (this.value ?: arrayListOf()).apply {
            this.removeAll(values)
        }

        this.value = value
    }

    fun <T> MutableLiveData<MutableList<T>>.removeAt(index: Int) {
        val value = (this.value ?: arrayListOf()).apply {
            this.removeAt(index)
        }

        this.value = value
    }

    fun <T> MutableLiveData<MutableList<T>>.remove(item: T) {
        val value = (this.value ?: arrayListOf()).apply {
            this.remove(item)
        }

        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.iterator(): Iterator<T> {
        return (value as MutableList<T>).iterator()
    }

    fun <T> MutableLiveData<MutableList<T>>.clear() {
        value = arrayListOf()
    }

    val <T> MutableLiveData<MutableList<T>>.indices get() = (value as MutableList<T>).indices

    val <T> MutableLiveData<MutableList<T>>.size get() = (value as MutableList<T>).size

    fun <T> MutableLiveData<MutableList<T>>.isEmpty(): Boolean {
        return (value as MutableList<T>).isEmpty()
    }

    fun <T> MutableLiveData<MutableList<T>>.isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun <T> MutableLiveData<MutableList<T>>.requireValue() = value!!

    @UiThread
    operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(values: List<T>) {
        val value = (this.value ?: arrayListOf()).apply {
            this.addAll(values)
        }

        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(v: T) {
        val value = (this.value ?: arrayListOf()).apply {
            this.add(v)
        }

        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.minusAssign(values: List<T>) {
        val value = (this.value ?: arrayListOf()).apply {
            this.removeAll(values)
        }

        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.minusAssign(v: T) {
        val value = (this.value ?: arrayListOf()).apply {
            this.remove(v)
        }

        this.value = value
    }

}