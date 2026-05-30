package dev.meloda.fast.ui.common

import androidx.compose.runtime.compositionLocalOf
import dev.meloda.fast.logger.FastLogger

val LocalLogger = compositionLocalOf { FastLogger.getInstance() }
