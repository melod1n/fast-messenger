package ru.melod1n.project.vkm.extensions

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

object ContextExtensions {

    fun Context.drawable(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(this, resId)
    }

    @ColorInt
    fun Context.color(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(this, resId)
    }

    fun Context.font(@FontRes resId: Int): Typeface? {
        return ResourcesCompat.getFont(this, resId)
    }

    fun Context.string(@StringRes resId: Int): String {
        return getString(resId)
    }

    fun Context.view(resId: Int, root: ViewGroup? = null, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(this).inflate(resId, root, attachToRoot)
    }


}