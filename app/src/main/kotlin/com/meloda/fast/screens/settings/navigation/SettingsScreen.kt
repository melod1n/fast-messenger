package com.meloda.fast.screens.settings.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.modules.auth.screens.logo.navigation.LogoScreen
import com.meloda.fast.screens.languagepicker.navigation.LanguagePickerScreen
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.screens.settings.presentation.SettingsRoute
import org.koin.compose.koinInject

object SettingsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val userSettings: UserSettings = koinInject()

        SettingsRoute(
            navigateToLogin = { navigator.replaceAll(LogoScreen) },
            navigateToLanguagePicker = { navigator.push(LanguagePickerScreen) },
            onBackClick = navigator::pop,
            onUseDarkThemeChanged = userSettings::useDarkThemeChanged,
            onUseAmoledThemeChanged = userSettings::useAmoledThemeChanged,
            onUseDynamicColorsChanged = userSettings::useDynamicColorsChanged,
            onUseBlurChanged = userSettings::useBlurChanged,
            onUseMultilineChanged = userSettings::useMultiline,
            onUseLongPollInBackgroundChanged = userSettings::setLongPollBackground,
            onOnlineChanged = userSettings::setOnline
        )
    }
}
