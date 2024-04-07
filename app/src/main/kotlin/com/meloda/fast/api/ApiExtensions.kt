package com.meloda.fast.api

object ApiExtensions {

    val Boolean.intString get() = (if (this) 1 else 0).toString()
}
