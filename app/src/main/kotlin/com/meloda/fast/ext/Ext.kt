//@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.meloda.fast.ext

import android.animation.ValueAnimator
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.net.MediaType
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

fun ValueAnimator.startWithIntValues(from: Int, to: Int) {
    setIntValues(from, to)
    start()
}

fun ValueAnimator.startWithFloatValues(from: Float, to: Float) {
    setFloatValues(from, to)
    start()
}

inline fun <T, K> Pair<T?, K?>.runIfElementsNotNull(block: (T, K) -> Unit) {
    val firstCopy = first
    val secondCopy = second
    if (firstCopy != null && secondCopy != null) {
        block(firstCopy, secondCopy)
    }
}

@Deprecated("get rid of LiveData")
fun <T> MutableLiveData<T>.requireValue(): T {
    return requireNotNull(this.value)
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
fun <T> Flow<T>.listenValue(action: suspend (T) -> Unit) {
    onEach {
        action.invoke(it)
    }.launchIn(viewModelScope)
}
