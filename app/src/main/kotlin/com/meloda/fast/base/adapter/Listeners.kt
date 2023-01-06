package com.meloda.fast.base.adapter

fun interface OnItemClickListener<T> {
    fun onItemClick(item: T)
}

fun interface OnItemLongClickListener<T> {
    fun onLongItemClick(item: T): Boolean
}
