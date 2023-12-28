package com.meloda.fast.screens.main.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.CheckPermission
import com.meloda.fast.ext.RequestPermission
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.conversations.navigation.ConversationsScreen
import com.meloda.fast.screens.login.navigation.LoginScreen
import com.meloda.fast.screens.main.MainViewModel
import com.meloda.fast.screens.main.model.MainScreenState
import com.meloda.fast.screens.settings.SettingsKeys

@Composable
fun HomeScreenContent(
    viewModel: MainViewModel
) {
    val navigator = LocalNavigator.currentOrThrow

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.accountsLoaded) {
        if (screenState.accounts.isNotEmpty() && UserConfig.isLoggedIn()) {
            navigator.replace(ConversationsScreen)
        } else {
            navigator.replace(LoginScreen)
        }
    }

    NotificationsPermissionChecker(
        screenState = screenState,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsPermissionChecker(
    screenState: MainScreenState,
    viewModel: MainViewModel
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val permission =
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

    if (screenState.openAppPermissions) {
        viewModel.onAppPermissionsOpened()

        LocalContext.current.apply {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", this.packageName, null)
                )
            )
        }
    }

    if (screenState.requestNotifications) {
        RequestPermission(permission = permission)
    }

    val isNeedToCheckNotificationsPermission by remember {
        derivedStateOf {
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    AppGlobal.preferences.getBoolean(
                        SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                        SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
                    ))
        }
    }

    if (isNeedToCheckNotificationsPermission) {
        CheckPermission(
            showRationale = {
                MaterialDialog(
                    title = UiText.Resource(R.string.warning),
                    message = UiText.Simple("The application will not be able to work properly without permission to send notifications."),
                    positiveText = UiText.Simple("Grant"),
                    positiveAction = {
                        viewModel.onRequestNotificationsPermissionClicked(true)
                    },
                    negativeText = UiText.Resource(R.string.cancel),
                    negativeAction = viewModel::onNotificationsAlertNegativeClicked,
                    onDismissAction = viewModel::onNotificationsAlertNegativeClicked,
                    buttonsInvokeDismiss = false
                )
            },
            onDenied = {
                MaterialDialog(
                    title = UiText.Resource(R.string.warning),
                    message = UiText.Simple("The application needs permission to send notifications to update messages and other information."),
                    positiveText = UiText.Simple("Grant"),
                    positiveAction = {
                        viewModel.onRequestNotificationsPermissionClicked(false)
                    },
                    negativeText = UiText.Resource(R.string.cancel),
                    onDismissAction = {},
                    buttonsInvokeDismiss = false
                )
            },
            permission = permission
        )
    }
}
