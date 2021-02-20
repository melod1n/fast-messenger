package com.meloda.fast.item

import android.graphics.drawable.Drawable
import android.view.View

data class SimpleMenuItem(
    val icon: Drawable?,
    val title: String,
    var clickListener: View.OnClickListener? = null
)
