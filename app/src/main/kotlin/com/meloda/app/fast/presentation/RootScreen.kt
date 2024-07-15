package com.meloda.app.fast.presentation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.meloda.app.fast.MainViewModel
import com.meloda.app.fast.MainViewModelImpl
import com.meloda.app.fast.auth.authNavGraph
import com.meloda.app.fast.auth.navigateToAuth
import com.meloda.app.fast.chatmaterials.navigation.chatMaterialsScreen
import com.meloda.app.fast.chatmaterials.navigation.navigateToChatMaterials
import com.meloda.app.fast.ui.R
import com.meloda.app.fast.languagepicker.navigation.languagePickerScreen
import com.meloda.app.fast.languagepicker.navigation.navigateToLanguagePicker
import com.meloda.app.fast.messageshistory.navigation.messagesHistoryScreen
import com.meloda.app.fast.messageshistory.navigation.navigateToMessagesHistory
import com.meloda.app.fast.navigation.Main
import com.meloda.app.fast.navigation.mainScreen
import com.meloda.app.fast.settings.navigation.navigateToSettings
import com.meloda.app.fast.settings.navigation.settingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootScreen(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
) {
    val context = LocalContext.current
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val isNeedToOpenAuth by viewModel.isNeedToReplaceWithAuth.collectAsStateWithLifecycle()
    val isNeedToShowDeniedDialog by viewModel.isNeedToShowNotificationsDeniedDialog.collectAsStateWithLifecycle()
    val isNeedToShowRationaleDialog by viewModel.isNeedToShowNotificationsRationaleDialog.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenAuth) {
        if (isNeedToOpenAuth) {
            viewModel.onNavigatedToAuth()
            navController.navigateToAuth(clearBackStack = true)
        }
    }

    if (isNeedToShowDeniedDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onNotificationsDeniedDialogDismissed,
            title = { Text(text = stringResource(id = R.string.warning)) },
            text = { Text(text = stringResource(id = R.string.background_long_poll_denied_text)) },
            confirmButton = {
                TextButton(onClick = viewModel::onNotificationsDeniedDialogConfirmClicked) {
                    Text(text = stringResource(id = R.string.action_request))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onNotificationsDeniedDialogCancelClicked) {
                    Text(text = stringResource(id = R.string.action_disable))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

    if (isNeedToShowRationaleDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onNotificationsRationaleDialogDismissed,
            title = { Text(text = stringResource(id = R.string.warning)) },
            text = { Text(text = stringResource(id = R.string.background_long_poll_rationale_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    }
                ) {
                    Text(text = stringResource(id = R.string.title_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onNotificationsRationaleDialogCancelClicked) {
                    Text(text = stringResource(id = R.string.action_disable))
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        )
    }

    if (startDestination != null) {
        NavHost(
            navController = navController,
            startDestination = requireNotNull(startDestination),
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            authNavGraph(
                onNavigateToMain = navController::navigateToMain,
                navController = navController
            )
            mainScreen(
                onError = viewModel::onError,
                onSettingsButtonClicked = navController::navigateToSettings,
                onConversationClicked = navController::navigateToMessagesHistory
            )

            messagesHistoryScreen(
                onError = viewModel::onError,
                onBack = navController::navigateUp,
                onChatMaterialsDropdownItemClicked = navController::navigateToChatMaterials
            )
            chatMaterialsScreen(
                onBack = navController::navigateUp
            )

            settingsScreen(
                onBack = navController::navigateUp,
                onLogOutButtonClicked = { navController.navigateToAuth(true) },
                onLanguageItemClicked = navController::navigateToLanguagePicker
            )
            languagePickerScreen(onBack = navController::navigateUp)
        }
    }
}

fun NavController.navigateToMain() {
    this.navigate(Main) {
        popUpTo(0) {
            inclusive = true
        }
    }
}
