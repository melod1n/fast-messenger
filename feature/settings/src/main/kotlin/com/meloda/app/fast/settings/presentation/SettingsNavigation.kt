package com.meloda.app.fast.settings.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.settings.model.NavigationAction
import kotlinx.serialization.Serializable

@Serializable
object Settings

fun NavGraphBuilder.settingsScreen(
    onBack: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToLanguagePicker: () -> Unit
) {
    composable<Settings> {
        SettingsScreen(
            onAction = { action ->
                when (action) {
                    NavigationAction.BackClick -> onBack()
                    NavigationAction.NavigateToLanguagePicker -> onNavigateToAuth()
                    NavigationAction.NavigateToLogin -> onNavigateToLanguagePicker()
                }
            }
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
