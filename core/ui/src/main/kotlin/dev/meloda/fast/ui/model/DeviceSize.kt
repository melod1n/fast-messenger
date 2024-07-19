package dev.meloda.fast.ui.model

sealed class DeviceSize {
    data object Small : DeviceSize()
    data object Compact : DeviceSize()
    data object Medium : DeviceSize()
    data object Expanded : DeviceSize()
}
