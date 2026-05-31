package dev.meloda.fast.ui.common

import androidx.compose.runtime.compositionLocalOf
import dev.meloda.fast.common.model.NetworkState

val LocalNetworkState = compositionLocalOf { NetworkState.DISCONNECTED }
