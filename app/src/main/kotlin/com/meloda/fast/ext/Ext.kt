package com.meloda.fast.ext

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.net.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Deprecated("use resources")
fun Int.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

@Deprecated("use resources")
fun Float.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

val MediaType.mimeType: String get() = "${type()}/${subtype()}"

@Throws(NullPointerException::class)
fun <T> T?.notNull(): T {
    return requireNotNull(this)
}


fun String?.orDots(count: Int = 3): String {
    return this ?: ("." * count)
}

private operator fun String.times(count: Int): String {
    val builder = StringBuilder()
    for (i in 0 until count) {
        builder.append(this)
    }

    return builder.toString()
}

inline fun <T> Iterable<T>.findIndex(predicate: (T) -> Boolean): Int? {
    return indexOf(firstOrNull(predicate)).let { if (it == -1) null else it }
}

inline fun <reified T, K, M : MutableMap<in K, T>> Iterable<T>.toMap(
    destination: M,
    keySelector: (T) -> K,
): M {
    for (element in this) {
        val key = keySelector(element)
        destination[key] = element
    }
    return destination
}

fun <T> MutableList<T>.addIf(element: T, condition: () -> Boolean) {
    if (condition.invoke()) add(element)
}

context(ViewModel)
fun <T> Flow<T>.listenValue(action: suspend (T) -> Unit) = listenValue(viewModelScope, action)

fun <T> Flow<T>.listenValue(
    coroutineScope: CoroutineScope,
    action: suspend (T) -> Unit
): Job = onEach(action::invoke).launchIn(coroutineScope)

fun String.toast(context: Context, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(context, this, duration).show()
}

context (Context)
fun String.toast(duration: Int = Toast.LENGTH_LONG) = toast(this@Context, duration)
