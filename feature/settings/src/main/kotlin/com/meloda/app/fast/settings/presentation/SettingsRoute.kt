package com.meloda.app.fast.settings.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Settings

fun NavGraphBuilder.settingsRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToLanguagePicker: () -> Unit
) {
    composable<Settings> {
        SettingsScreen(
            onError = onError,
            onBack = onBack,
            onNavigateToAuth = onNavigateToAuth,
            onNavigateToLanguagePicker = onNavigateToLanguagePicker
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
