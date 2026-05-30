package dev.meloda.fast.settings.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.settings.SettingsViewModel
import dev.meloda.fast.settings.model.SettingsEffect
import dev.meloda.fast.settings.model.SettingsNavigationIntent
import dev.meloda.fast.settings.presentation.SettingsRoute
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Settings

fun NavGraphBuilder.settingsScreen(
    handleNavigationIntent: (SettingsNavigationIntent) -> Unit
) {
    composable<Settings> {
        val view = LocalView.current
        val viewModel: SettingsViewModel = koinViewModel()
        val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.screenEffectFlow.onEach { effect ->
                when (effect) {
                    is SettingsEffect.Navigate -> handleNavigationIntent(effect.intent)
                    is SettingsEffect.PerformHaptic -> {
                        if (AppSettings.General.enableHaptic) {
                            view.performHapticFeedback(effect.type.getHaptic())
                        }
                    }
                }
            }.collect()
        }

        SettingsRoute(
            handleIntent = viewModel::handleIntent,
            screenState = screenState,
        )
    }
}

fun NavController.navigateToSettings() {
    this.navigate(Settings)
}
