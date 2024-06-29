package com.meloda.app.fast.settings.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.settings.model.NavigationAction
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
            onAction = { action ->
                when (action) {
                    NavigationAction.BackClick -> onBack()
                    NavigationAction.NavigateToLanguagePicker -> onNavigateToLanguagePicker()
                    NavigationAction.NavigateToAuth -> onNavigateToAuth()
                }
            }
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
