package com.meloda.fast.model.base

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource

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

    @Composable
    fun getImage(): Any {
        return when (this) {
            is Color -> ColorDrawable(color)
            is ColorResource -> ColorDrawable(colorResource(id = resId).toArgb())
            is Resource -> painterResource(id = resId)
            is Simple -> drawable
            is Url -> url
        }
    }

    @Composable
    fun getResourcePainter(): Painter? {
        return when (this) {
            is Resource -> painterResource(id = resId)
            else -> null
        }
    }
}
