package dev.meloda.fast.ui.common

import androidx.compose.runtime.compositionLocalOf
import dev.meloda.fast.ui.model.DeviceSize
import dev.meloda.fast.ui.model.SizeConfig

val LocalSizeConfig = compositionLocalOf {
    SizeConfig(
        widthSize = DeviceSize.Compact,
        heightSize = DeviceSize.Compact
    )
}
