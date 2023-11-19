package com.meloda.fast.screens.settings.model

import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.isUsingDynamicColors

data class AppTheme(
    val usingDarkStyle: Boolean,
    val usingDynamicColors: Boolean
) {

    companion object {
        val EMPTY: AppTheme = AppTheme(
            usingDarkStyle = isUsingDarkTheme(),
            usingDynamicColors = isUsingDynamicColors()
        )
    }
}
