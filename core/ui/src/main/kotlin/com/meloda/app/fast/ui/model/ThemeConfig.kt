package com.meloda.app.fast.ui.model

data class ThemeConfig(
    val darkMode: Boolean,
    val dynamicColors: Boolean,
    val selectedColorScheme: Int,
    val amoledDark: Boolean,
    val enableBlur: Boolean,
    val enableMultiline: Boolean,
    val isDeviceCompact: Boolean
)
