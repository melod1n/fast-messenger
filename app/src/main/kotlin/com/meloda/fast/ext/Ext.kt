package com.meloda.fast.ext

import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.net.MediaType
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.AndroidUtils
import kotlinx.coroutines.*
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
fun <T> T?.notNull(lazyMessage: (() -> Any)? = null): T {
    return if (lazyMessage != null) {
        requireNotNull(this, lazyMessage)
    } else {
        requireNotNull(this)
    }
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

fun isUsingBlur(): Boolean =
    AppGlobal.preferences.getBoolean(
        SettingsFragment.KEY_USE_BLUR,
        SettingsFragment.DEFAULT_VALUE_USE_BLUR
    )

fun isUsingCompose(): Boolean =
    AppGlobal.preferences.getBoolean(
        SettingsFragment.KEY_USE_COMPOSE,
        SettingsFragment.DEFAULT_VALUE_USE_COMPOSE
    )

fun createTimerFlow(
    time: Int,
    onStartAction: suspend () -> Unit,
    onTickAction: suspend (remainedTime: Int) -> Unit,
    onTimeoutAction: suspend () -> Unit,
    interval: Duration = 1.seconds
): Flow<Int> = (time downTo 0)
    .asSequence()
    .asFlow()
    .onStart { onStartAction() }
    .onEach { timeLeft ->
        onTickAction(timeLeft)
        if (timeLeft == 0) {
            onTimeoutAction()
        } else {
            delay(interval)
        }
    }

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

context(ViewModel)
fun <T> MutableSharedFlow<T>.emitOnMainScope(value: T) = emitOnScope(value, Dispatchers.Main)

context(ViewModel)
fun <T> MutableSharedFlow<T>.emitOnScope(
    value: T,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    viewModelScope.launch(dispatcher) {
        emit(value)
    }
}

context(CoroutineScope)
        suspend fun <T> MutableSharedFlow<T>.emitWithMain(value: T) {
    withContext(Dispatchers.Main) {
        emit(value)
    }
}
