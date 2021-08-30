package com.meloda.fast.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

object DrawableExtensions {

    fun Drawable?.tint(@ColorInt color: Int): Drawable? {
        this?.setTint(color)
        return this
    }

}