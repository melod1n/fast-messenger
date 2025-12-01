package dev.meloda.fast.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal

@Composable
fun <T> ProvidableCompositionLocal<T?>.getOrThrow(): T {
    return requireNotNull(current)
}
