package dev.meloda.fast.settings.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.settings.SettingsViewModel
import dev.meloda.fast.settings.model.SettingsNavigationIntent
import dev.meloda.fast.settings.presentation.SettingsRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Settings

fun NavGraphBuilder.settingsScreen(
    handleNavigationIntent: (SettingsNavigationIntent) -> Unit
) {
    composable<Settings> {
        val viewModel: SettingsViewModel = koinViewModel()
        val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()
        val hapticType by viewModel.hapticTypeFlow.collectAsStateWithLifecycle()
        val navigationIntent by viewModel.navigationIntentFlow.collectAsStateWithLifecycle()

        LaunchedEffect(navigationIntent) {
            navigationIntent?.let(handleNavigationIntent)
        }

        SettingsRoute(
            handleIntent = viewModel::handleIntent,
            screenState = screenState,
            hapticType = hapticType
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
