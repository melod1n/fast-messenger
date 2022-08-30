package com.meloda.fast.util

import android.graphics.Color

object ColorUtils {


    fun alphaColor(color: Int, alphaFactor: Float): Int {
        val alpha = Color.alpha(color)

        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)

        return Color.argb((alpha * alphaFactor).toInt(), red, green, blue)
    }

}