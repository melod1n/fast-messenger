package com.meloda.fast.extensions

fun Boolean.toApiStyle() = (if (this) 1 else 0).toString()