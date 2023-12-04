package com.meloda.fast.screens.settings.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.login.navigation.LoginScreen
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.screens.settings.presentation.SettingsRoute
import com.meloda.fast.screens.updates.navigation.UpdatesScreen
import org.koin.compose.koinInject

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userSettings: UserSettings = koinInject()

        SettingsRoute(
            navigateToUpdates = {
                navigator.push(UpdatesScreen)
            },
            navigateToLogin = {
                navigator.replaceAll(LoginScreen)
            },
            onBackClick = navigator::pop,
            onUseDarkThemeChanged = userSettings::useDarkThemeChanged,
            onUseDynamicColorsChanged = userSettings::useDynamicColorsChanged,
            onUseMultilineChanged = userSettings::useMultiline
        )
    }
}
