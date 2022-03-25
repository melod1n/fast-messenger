package com.meloda.fast.base

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

abstract class ResourceManager(protected val context: Context) {

    protected fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    @ColorInt
    protected fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

}