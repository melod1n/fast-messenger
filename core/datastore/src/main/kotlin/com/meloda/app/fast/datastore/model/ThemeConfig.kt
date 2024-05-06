package com.meloda.app.fast.datastore.model

data class ThemeConfig(
    val usingDarkStyle: Boolean,
    val usingDynamicColors: Boolean,
    val usingAmoledBackground: Boolean,
    val usingBlur: Boolean
) {


    // TODO: 05/05/2024, Danil Nikolaev: get current values from settings
    companion object {
        val EMPTY: ThemeConfig = ThemeConfig(
            usingDarkStyle = false,
            usingDynamicColors = false,
            usingAmoledBackground = false,
            usingBlur = false
        )
    }
}
