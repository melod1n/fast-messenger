package com.meloda.app.fast.common

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

sealed class UiImage {

    data class Resource(@DrawableRes val resId: Int) : UiImage()

    data class Simple(val drawable: Drawable) : UiImage()

    data class Color(@ColorInt val color: Int) : UiImage()

    data class ColorResource(@ColorRes val resId: Int) : UiImage()

    data class Url(val url: String) : UiImage()

    fun extractUrl(): String? = when (this) {
        is Url -> this.url
        else -> null
    }
}
