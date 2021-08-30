package com.meloda.fast.util

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import com.meloda.fast.R

object ColorUtils {

    @ColorInt
    fun getColorAccent(context: Context): Int {
        return AndroidUtils.getThemeAttrColor(context, R.attr.colorAccent)
    }

    @ColorInt
    fun getColorPrimary(context: Context): Int {
        return AndroidUtils.getThemeAttrColor(context, R.attr.colorPrimary)
    }

    @JvmOverloads
    fun darkenColor(color: Int, darkFactor: Float = 0.75f): Int {
        var newColor = color
        val hsv = FloatArray(3)
        Color.colorToHSV(newColor, hsv)
        hsv[2] *= darkFactor
        newColor = Color.HSVToColor(hsv)
        return newColor
    }

}