package com.meloda.fast.util

object TextUtils {

    fun getFirstLetterFromString(string: String): String {
        for (i in string.indices) {
            val char = string[i]

            if (char.isLetter()) return char.toString()
        }

        return ""
    }

}