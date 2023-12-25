package com.meloda.fast.screens.main.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.isUsingDynamicColors
import com.meloda.fast.model.AppAccount

@Immutable
data class MainScreenState(
    val accounts: List<AppAccount>,
    val accountsLoaded: Boolean,
    val useDarkTheme: Boolean,
    val useDynamicColors: Boolean,
    val requestNotifications: Boolean,
    val openAppPermissions: Boolean
) {

    companion object {
        val EMPTY: MainScreenState = MainScreenState(
            accounts = emptyList(),
            accountsLoaded = false,
            useDarkTheme = isUsingDarkTheme(),
            useDynamicColors = isUsingDynamicColors(),
            requestNotifications = false,
            openAppPermissions = false
        )
    }
}
