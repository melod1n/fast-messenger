package dev.meloda.fast.settings.presentation

import androidx.compose.runtime.Composable
import dev.meloda.fast.settings.model.HapticType
import dev.meloda.fast.settings.model.SettingsIntent
import dev.meloda.fast.settings.model.SettingsScreenState

@Composable
fun SettingsRoute(
    handleIntent: (SettingsIntent) -> Unit,
    screenState: SettingsScreenState,
    hapticType: HapticType?
) {
    SettingsScreen(
        handleIntent = handleIntent,
        screenState = screenState,
        hapticType = hapticType
    )

    HandleDialogs(
        handleIntent = handleIntent,
        screenState = screenState,
    )
}
