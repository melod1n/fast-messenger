package com.meloda.fast.model.base

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes

sealed class Image {

    data class Resource(@DrawableRes val resId: Int) : Image()

    data class Simple(val drawable: Drawable) : Image()

    data class Url(val url: String) : Image()
}
