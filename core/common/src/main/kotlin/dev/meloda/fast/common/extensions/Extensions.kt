package dev.meloda.fast.common.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun Context.restartApp() {
    (this as? Activity)?.let { activity ->
        activity.finishAffinity()
        activity.startActivity(
            Intent(
                this,
                Class.forName("dev.meloda.fast.MainActivity")
            )
        )
    }
}

inline fun <T> Iterable<T>.findWithIndex(predicate: (T) -> Boolean): Pair<Int, T>? {
    val value = firstOrNull(predicate) ?: return null
    return indexOf(value).let { index -> if (index == -1) null else index to value }
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

fun createTimerFlow(
    time: Int,
    onStartAction: (suspend () -> Unit)? = null,
    onTickAction: (suspend (remainedTime: Int) -> Unit)? = null,
    onTimeoutAction: (suspend () -> Unit)? = null,
    interval: Duration = 1.seconds
): Flow<Int> = (time downTo 0)
    .asSequence()
    .asFlow()
    .onStart { onStartAction?.invoke() }
    .onEach { timeLeft ->
        onTickAction?.invoke(timeLeft)
        if (timeLeft == 0) {
            onTimeoutAction?.invoke()
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
fun <T> MutableStateFlow<T>.updateValue(newValue: T) = this.update { newValue }

fun <T> MutableStateFlow<T>.setValue(function: (T) -> T) {
    val newValue = function(value)
    update { newValue }
}

fun Any.asInt(): Int {
    return when (this) {
        is Number -> this.toInt()

        else -> throw IllegalArgumentException("Object is not numeric")
    }
}

fun <T> Any.toList(mapper: (old: Any) -> T): List<T> {
    return when (this) {
        is List<*> -> this.mapNotNull { it?.run(mapper) }

        else -> emptyList()
    }
}

@ChecksSdkIntAtLeast(parameter = 0, lambda = 1)
fun isSdkAtLeast(sdkInt: Int, action: (() -> Unit)? = null): Boolean {
    return if (Build.VERSION.SDK_INT >= sdkInt) {
        action?.invoke()
        true
    } else {
        false
    }
}
