package com.meloda.fast.screens.main.model

import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.isUsingDynamicColors
import com.meloda.fast.model.AppAccount

data class MainScreenState(
    val accounts: List<AppAccount>,
    val accountsLoaded: Boolean,
    val useDarkTheme: Boolean,
    val useDynamicColors: Boolean
) {

    companion object {
        val EMPTY: MainScreenState = MainScreenState(
            accounts = emptyList(),
            accountsLoaded = false,
            useDarkTheme = isUsingDarkTheme(),
            useDynamicColors = isUsingDynamicColors()
        )
    }
}
