package dev.meloda.fast.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.Modifier

@Composable
fun <T> ProvidableCompositionLocal<T?>.getOrThrow(): T {
    return requireNotNull(current)
}

inline fun Modifier.ifTrue(
    condition: Boolean,
    block: Modifier.() -> Modifier
): Modifier = if (condition) block() else this
