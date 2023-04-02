package com.meloda.fast.ext

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.common.net.MediaType
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.AndroidUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Deprecated("use resources or rewrite in Compose")
fun Int.dpToPx(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return (this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
}

@Deprecated("use resources or rewrite in Compose")
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


fun isUsingDarkTheme(): Boolean {
    val nightThemeMode = AppGlobal.preferences.getInt(
        SettingsFragment.KEY_APPEARANCE_DARK_THEME,
        SettingsFragment.DEFAULT_VALUE_APPEARANCE_DARK_THEME
    )
    val appForceDarkMode = nightThemeMode == AppCompatDelegate.MODE_NIGHT_YES
    val appBatterySaver = nightThemeMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

    val systemUiNightMode = AppGlobal.resources.configuration.uiMode

    val isSystemBatterySaver = AndroidUtils.isBatterySaverOn()
    val isSystemUsingDarkTheme =
        systemUiNightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    return appForceDarkMode || (appBatterySaver && isSystemBatterySaver) || (!appBatterySaver && isSystemUsingDarkTheme && nightThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

fun isUsingDynamicColors(): Boolean =
    AppGlobal.preferences.getBoolean(
        SettingsFragment.KEY_USE_DYNAMIC_COLORS,
        SettingsFragment.DEFAULT_VALUE_USE_DYNAMIC_COLORS
    )

fun createTimerFlow(
    isNeedToEndCondition: suspend () -> Boolean,
    onStartAction: (suspend () -> Unit)? = null,
    onTickAction: (suspend () -> Unit)? = null,
    onEndAction: (suspend () -> Unit)? = null,
    interval: Duration = 1.seconds
): Flow<Boolean> = flow {
    while (true) {
        val isNeedToEnd = isNeedToEndCondition()
        emit(isNeedToEnd)
        if (isNeedToEnd) break
    }
}
    .onStart { onStartAction?.invoke() }
    .onEach { isNeedToEnd ->
        onTickAction?.invoke()
        if (isNeedToEnd) {
            onEndAction?.invoke()
        } else {
            delay(interval)
        }
    }
