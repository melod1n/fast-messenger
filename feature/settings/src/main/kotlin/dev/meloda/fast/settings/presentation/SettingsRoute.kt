package dev.meloda.fast.settings.presentation

import androidx.compose.runtime.Composable
import dev.meloda.fast.settings.model.SettingsIntent
import dev.meloda.fast.settings.model.SettingsScreenState

@Composable
fun SettingsRoute(
    handleIntent: (SettingsIntent) -> Unit,
    screenState: SettingsScreenState,
) {
    SettingsScreen(
        handleIntent = handleIntent,
        screenState = screenState,
    )

    HandleDialogs(
        handleIntent = handleIntent,
        screenState = screenState,
    )
}
