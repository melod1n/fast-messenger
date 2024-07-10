package com.meloda.app.fast.designsystem

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun LocalContentAlpha(
    defaultColor: Color = MaterialTheme.colorScheme.onBackground,
    alpha: Float,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalContentColor provides defaultColor.copy(alpha = alpha)
    ) {
        content()
    }
}
