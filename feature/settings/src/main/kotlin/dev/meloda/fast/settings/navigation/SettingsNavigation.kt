package dev.meloda.fast.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.settings.presentation.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
object Settings

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onLogOutButtonClicked: () -> Unit,
    onLanguageItemClicked: () -> Unit,
    onRestartRequired: () -> Unit,
) {
    composable<Settings> {
        SettingsRoute(
            onBack = onBack,
            onLogOutButtonClicked = onLogOutButtonClicked,
            onLanguageItemClicked = onLanguageItemClicked,
            onRestartRequired = onRestartRequired
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
