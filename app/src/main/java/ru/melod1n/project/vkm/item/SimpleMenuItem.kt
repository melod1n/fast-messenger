package ru.melod1n.project.vkm.item

import android.graphics.drawable.Drawable
import android.view.View

data class SimpleMenuItem(val icon: Drawable?, val title: String, var clickListener: View.OnClickListener? = null)
