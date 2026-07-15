package dev.meloda.fast.common.model

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

@Immutable
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

    fun extractResId(): Int = when (this) {
        is Resource -> this.resId
        else -> throw IllegalStateException("this UiImage is not Resource")
    }
}
