package com.meloda.fast.extensions

import java.util.*

object StringExtensions {

    fun String.lowerCase(): String {
        return toLowerCase(Locale.getDefault())
    }

}