package com.meloda.app.fast.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.settings.presentation.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
object Settings

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onLogOutButtonClicked: () -> Unit,
    onLanguageItemClicked: () -> Unit
) {
    composable<Settings> {
        SettingsRoute(
            onBack = onBack,
            onLogOutButtonClicked = onLogOutButtonClicked,
            onLanguageItemClicked = onLanguageItemClicked
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
