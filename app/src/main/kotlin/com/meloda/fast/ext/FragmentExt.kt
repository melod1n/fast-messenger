package com.meloda.fast.ext

import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString
import kotlinx.coroutines.flow.Flow

context(Fragment)
fun <T> Flow<T>.listenValue(
    action: suspend (T) -> Unit
) = listenValue(lifecycleScope, action)


context(Fragment)
fun String.toast(duration: Int = Toast.LENGTH_LONG) = toast(requireContext(), duration)

context(Fragment)
fun color(@ColorRes resId: Int): Int {
    return ContextCompat.getColor(requireContext(), resId)
}

context(Fragment)
fun drawable(@DrawableRes resId: Int): Drawable? {
    return ContextCompat.getDrawable(requireContext(), resId)
}

context(Fragment)
fun string(@StringRes resId: Int): String {
    return getString(resId)
}

context(Fragment)
fun string(@StringRes resId: Int, vararg args: Any?): String {
    return getString(resId, *args)
}

context(Fragment)
fun UiText?.asString(): String? {
    return this.parseString(this@Fragment.requireContext())
}
