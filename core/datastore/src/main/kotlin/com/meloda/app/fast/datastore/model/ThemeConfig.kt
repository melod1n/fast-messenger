package com.meloda.app.fast.datastore.model

data class ThemeConfig(
    val usingDarkStyle: Boolean,
    val usingDynamicColors: Boolean,
    val selectedColorScheme: Int,
    val usingAmoledBackground: Boolean,
    val usingBlur: Boolean,
    val multiline: Boolean
)
