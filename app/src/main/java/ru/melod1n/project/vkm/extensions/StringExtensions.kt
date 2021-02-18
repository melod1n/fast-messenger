package ru.melod1n.project.vkm.extensions

import java.util.*

object StringExtensions {

    fun String.lowerCase(): String {
        return toLowerCase(Locale.getDefault())
    }

}