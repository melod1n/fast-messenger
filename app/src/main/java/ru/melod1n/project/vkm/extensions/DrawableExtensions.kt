package ru.melod1n.project.vkm.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

object DrawableExtensions {

    fun Drawable?.tint(@ColorInt color: Int): Drawable? {
        this?.setTint(color)
        return this
    }

}