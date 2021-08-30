package com.meloda.fast.extensions

import kotlin.math.roundToInt

object FloatExtensions {

    fun Float.int(): Int {
        return roundToInt()
    }

}