package com.meloda.app.fast.ui.model

data class ThemeConfig(
    val usingDarkStyle: Boolean,
    val usingDynamicColors: Boolean,
    val selectedColorScheme: Int,
    val usingAmoledBackground: Boolean,
    val usingBlur: Boolean,
    val isMultiline: Boolean,
    val isDeviceCompact: Boolean
)
