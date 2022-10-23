package com.meloda.fast.ext

inline fun String?.ifEmpty(defaultValue: () -> String?): String? =
    if (this?.isEmpty() == true) defaultValue() else this