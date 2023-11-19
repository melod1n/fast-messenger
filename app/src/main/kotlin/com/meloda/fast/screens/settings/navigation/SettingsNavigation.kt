package com.meloda.fast.screens.settings.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.login.navigation.LoginNavigation
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.screens.settings.presentation.SettingsRoute
import org.koin.compose.koinInject

object SettingsNavigation : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userSettings: UserSettings = koinInject()

        SettingsRoute(
            navigateToLogin = {
                navigator.replaceAll(LoginNavigation)
            },
            onBackClick = {
                navigator.pop()
            },
            onUseDarkThemeChanged = userSettings::useDarkThemeChanged,
            onUseDynamicColorsChanged = userSettings::useDynamicColorsChanged,
        )
    }
}
