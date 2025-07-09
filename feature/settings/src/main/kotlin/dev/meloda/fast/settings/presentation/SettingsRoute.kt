package dev.meloda.fast.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.datastore.SettingsKeys
import dev.meloda.fast.settings.SettingsViewModel
import dev.meloda.fast.settings.model.SettingsDialog
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onLogOutButtonClicked: () -> Unit,
    onLanguageItemClicked: () -> Unit,
    onRestartRequired: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val hapticType by viewModel.hapticType.collectAsStateWithLifecycle()
    val dialog by viewModel.dialog.collectAsStateWithLifecycle()
    val isNeedToRestart by viewModel.isNeedToRestart.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToRestart) {
        if (isNeedToRestart) {
            onRestartRequired()
        }
    }

    SettingsScreen(
        screenState = screenState,
        hapticType = hapticType,
        onBack = onBack,
        onHapticPerformed = viewModel::onHapticPerformed,
        onSettingsItemClicked = { key ->
            when (key) {
                SettingsKeys.KEY_APPEARANCE_LANGUAGE -> {
                    onLanguageItemClicked()
                }

                else -> viewModel.onSettingsItemClicked(key)
            }
        },
        onSettingsItemLongClicked = viewModel::onSettingsItemLongClicked,
        onSettingsItemValueChanged = viewModel::onSettingsItemChanged
    )

    HandleDialogs(
        screenState = screenState,
        dialog = dialog,
        onConfirmed = { dialog, bundle ->
            when (dialog) {
                is SettingsDialog.LogOut -> {
                    onLogOutButtonClicked()
                }

                else -> Unit
            }
            viewModel.onDialogConfirmed(dialog, bundle)
        },
        onDismissed = viewModel::onDialogDismissed,
        onItemPicked = viewModel::onDialogItemPicked
    )
}
