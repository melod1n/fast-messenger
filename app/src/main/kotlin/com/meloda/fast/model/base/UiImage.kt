package com.meloda.fast.model.base

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.meloda.fast.ext.GlideParams
import com.meloda.fast.ext.ImageLoader.loadWithGlide

sealed class UiImage {

    data class Resource(@DrawableRes val resId: Int) : UiImage()

    data class Simple(val drawable: Drawable) : UiImage()

    data class Color(@ColorInt val color: Int) : UiImage()

    data class ColorResource(@ColorRes val resId: Int) : UiImage()

    data class Url(val url: String) : UiImage()
}

fun ImageView.setImage(image: UiImage, glideBlock: GlideParams.() -> Unit) {
    val glideParams = GlideParams()
    glideBlock.invoke(glideParams)
    this.setImage(image, glideParams)
}

fun ImageView.setImage(image: UiImage, glideParams: GlideParams? = null) {
    image.attachTo(this, glideParams)
}

fun UiImage?.attachTo(imageView: ImageView, glideBlock: GlideParams.() -> Unit) {
    val glideParams = GlideParams()
    glideBlock.invoke(glideParams)
    this.attachTo(imageView, glideParams)
}

fun UiImage?.attachTo(imageView: ImageView, glideParams: GlideParams? = null) {
    when (this) {
        is UiImage.Simple -> imageView.setImageDrawable(drawable)
        is UiImage.Resource -> imageView.setImageResource(resId)
        is UiImage.Color -> imageView.setImageDrawable(ColorDrawable(color))
        is UiImage.ColorResource -> imageView.setImageDrawable(
            ColorDrawable(ContextCompat.getColor(imageView.context, resId))
        )
        is UiImage.Url -> glideParams?.let { params ->
            params.imageUrl = url
            imageView.loadWithGlide(params)
        }
        else -> Unit
    }
}

fun UiImage?.asDrawable(context: Context): Drawable? {
    return when (this) {
        is UiImage.Simple -> drawable
        is UiImage.Resource -> ContextCompat.getDrawable(context, resId)
        is UiImage.Color -> ColorDrawable(color)
        is UiImage.ColorResource -> ColorDrawable(ContextCompat.getColor(context, resId))
        else -> null
    }
}
