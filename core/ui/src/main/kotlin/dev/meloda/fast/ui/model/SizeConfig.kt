package dev.meloda.fast.ui.model

data class SizeConfig(
    val widthSize: DeviceSize,
    val heightSize: DeviceSize
) {

    val isHeightSmall: Boolean get() = heightSize is DeviceSize.Small
    val isWidthSmall: Boolean get() = widthSize is DeviceSize.Small
}
