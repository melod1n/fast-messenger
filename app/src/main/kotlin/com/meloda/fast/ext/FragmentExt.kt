package com.meloda.fast.ext

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

context (AppCompatActivity)
fun <T> Flow<T>.listenValue(action: suspend (T) -> Unit) {
    onEach {
        action.invoke(it)
    }.launchIn(lifecycleScope)
}

context(Fragment)
fun <T> Flow<T>.listenValue(action: suspend (T) -> Unit) {
    onEach {
        action.invoke(it)
    }.launchIn(viewLifecycleOwner.lifecycleScope)
}

context (Context)
fun String.toast(duration: Int = Toast.LENGTH_LONG) {
    this.toast(this@Context, duration)
}

context(Fragment)
fun String.toast(duration: Int = Toast.LENGTH_LONG) {
    this.toast(this@Fragment.requireContext(), duration)
}

fun String.toast(context: Context, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(context, this, duration).show()
}

context(Fragment)
fun color(@ColorRes resId: Int): Int {
    return ContextCompat.getColor(requireContext(), resId)
}

context(Fragment)
fun drawable(@DrawableRes resId: Int): Drawable? {
    return ContextCompat.getDrawable(requireContext(), resId)
}

context (Fragment)
fun string(@StringRes resId: Int): String {
    return getString(resId)
}
