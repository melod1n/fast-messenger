package com.meloda.fast.ext

val Boolean?.isTrue: Boolean get() = this != null && this == true

val Boolean?.isFalse: Boolean get() = this != null && this == false
