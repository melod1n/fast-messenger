package com.meloda.fast.util

import android.content.Context
import androidx.annotation.ColorInt
import com.meloda.fast.R

object ColorUtils {

    @ColorInt
    fun getColorAccent(context: Context): Int {
        return AndroidUtils.getThemeAttrColor(context, R.attr.colorAccent)
    }

}