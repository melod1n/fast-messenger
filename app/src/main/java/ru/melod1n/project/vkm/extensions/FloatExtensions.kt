package ru.melod1n.project.vkm.extensions

import kotlin.math.roundToInt

object FloatExtensions {

    fun Float.int(): Int {
        return roundToInt()
    }

}