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

sealed class Image {

    data class Resource(@DrawableRes val resId: Int) : Image()

    data class Simple(val drawable: Drawable) : Image()

    data class Color(@ColorInt val color: Int) : Image()

    data class ColorResource(@ColorRes val resId: Int) : Image()

    data class Url(val url: String) : Image()
}

fun ImageView.setImage(image: Image, glideBlock: GlideParams.() -> Unit) {
    val glideParams = GlideParams()
    glideBlock.invoke(glideParams)
    this.setImage(image, glideParams)
}

fun ImageView.setImage(image: Image, glideParams: GlideParams? = null) {
    image.attachTo(this, glideParams)
}

fun Image?.attachTo(imageView: ImageView, glideBlock: GlideParams.() -> Unit) {
    val glideParams = GlideParams()
    glideBlock.invoke(glideParams)
    this.attachTo(imageView, glideParams)
}

fun Image?.attachTo(imageView: ImageView, glideParams: GlideParams? = null) {
    when (this) {
        is Image.Simple -> imageView.setImageDrawable(drawable)
        is Image.Resource -> imageView.setImageResource(resId)
        is Image.Color -> imageView.setImageDrawable(ColorDrawable(color))
        is Image.ColorResource -> imageView.setImageDrawable(
            ColorDrawable(ContextCompat.getColor(imageView.context, resId))
        )
        is Image.Url -> glideParams?.let { params ->
            params.imageUrl = url
            imageView.loadWithGlide(params)
        }
        else -> Unit
    }
}

fun Image?.asDrawable(context: Context): Drawable? {
    return when (this) {
        is Image.Simple -> drawable
        is Image.Resource -> ContextCompat.getDrawable(context, resId)
        is Image.Color -> ColorDrawable(color)
        is Image.ColorResource -> ColorDrawable(ContextCompat.getColor(context, resId))
        else -> null
    }
}
