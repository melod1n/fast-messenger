package com.meloda.fast.screens.main

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.CheckPermission
import com.meloda.fast.ext.RequestPermission
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.conversations.navigation.ConversationsScreen
import com.meloda.fast.screens.login.navigation.LoginScreen
import com.meloda.fast.screens.main.model.MainScreenState
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.service.LongPollService
import com.meloda.fast.service.OnlineService
import com.meloda.fast.ui.AppTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext


class MainActivity : ComponentActivity() {

    private val userSettings: UserSettings by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannels()

        userSettings.longPollBackground
            .onEach { value ->
                toggleLongPollService(true, value)
            }.launchIn(lifecycleScope)

        userSettings.online
            .onEach(::toggleOnlineService)
            .launchIn(lifecycleScope)

        setContent {
            MainScreen()
        }
    }

    private fun createNotificationChannels() {
        isSdkAtLeast(Build.VERSION_CODES.O) {
            val dialogsName = "Dialogs"
            val dialogsDescriptionText = "Channel for dialogs notifications"
            val dialogsImportance = NotificationManager.IMPORTANCE_HIGH
            val dialogsChannel =
                NotificationChannel("simple_notifications", dialogsName, dialogsImportance).apply {
                    description = dialogsDescriptionText
                }

            val longPollName = "Long Polling"
            val longPollDescriptionText = "Channel for long polling service"
            val longPollImportance = NotificationManager.IMPORTANCE_NONE
            val longPollChannel =
                NotificationChannel("long_polling", longPollName, longPollImportance).apply {
                    description = longPollDescriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannels(listOf(dialogsChannel, longPollChannel))
        }
    }

    @Composable
    fun MainScreen() {
        KoinContext {
            val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()
            val theme by userSettings.themeFlow.collectAsStateWithLifecycle()

            AppTheme(
                useDarkTheme = theme.usingDarkStyle,
                useDynamicColors = theme.usingDynamicColors
            ) {
                Navigator(screen = HomeScreen(viewModel = viewModel))

                NotificationsPermissionChecker(
                    screenState = screenState,
                    viewModel = viewModel
                )
            }
        }
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

    private fun toggleLongPollService(
        enable: Boolean,
        asForeground: Boolean = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )
    ) {
        if (enable) {
            val longPollIntent = Intent(this, LongPollService::class.java)

            if (asForeground) {
                ContextCompat.startForegroundService(this, longPollIntent)
            } else {
                startService(longPollIntent)
            }
        } else {
            stopService(Intent(this, LongPollService::class.java))
        }
    }

    private fun toggleOnlineService(enable: Boolean) {
        if (enable) {
            startService(Intent(this, OnlineService::class.java))
        } else {
            stopService(Intent(this, OnlineService::class.java))
        }
    }

    private fun stopServices() {
        toggleOnlineService(enable = false)

        val asForeground = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )

        if (!asForeground) {
            toggleLongPollService(enable = false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
    }
}

data class HomeScreen(val viewModel: MainViewModel) : AndroidScreen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val screenState by viewModel.screenState.collectAsStateWithLifecycle()

        if (screenState.accountsLoaded) {
            if (screenState.accounts.isNotEmpty() && UserConfig.isLoggedIn()) {
                navigator.replace(ConversationsScreen)
            } else {
                navigator.replace(LoginScreen)
            }
        }
    }
}
