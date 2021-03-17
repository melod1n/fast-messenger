package com.meloda.extensions

import kotlin.math.roundToInt

object FloatExtensions {

    fun Float.int(): Int {
        return roundToInt()
    }

}