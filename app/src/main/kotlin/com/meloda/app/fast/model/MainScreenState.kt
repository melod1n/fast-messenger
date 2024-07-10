package com.meloda.app.fast.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.model.database.AccountEntity

@Immutable
data class MainScreenState(
    val accounts: List<AccountEntity>,
    val accountsLoaded: Boolean,
    val useDarkTheme: Boolean,
    val useDynamicColors: Boolean,
    val isNeedToRequestNotifications: Boolean,
    val isNeedToOpenAppPermissions: Boolean,
    val isNeedToOpenAuth: Boolean,
) {

    companion object {
        val EMPTY: MainScreenState = MainScreenState(
            accounts = emptyList(),
            accountsLoaded = false,

            // TODO: 05/05/2024, Danil Nikolaev: implement
            useDarkTheme = false,
            useDynamicColors = false,
            isNeedToRequestNotifications = false,
            isNeedToOpenAppPermissions = false,
            isNeedToOpenAuth = false,
        )
    }
}
