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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannels()

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

    private fun checkExternalLanguageUpdate(userSettings: UserSettings) {
        val currentSavedLanguage = userSettings.language.value

        val appLocales = AppCompatDelegate.getApplicationLocales()

        if (!appLocales.toLanguageTags().startsWith(currentSavedLanguage)) {
            val newLanguage = if (appLocales.isEmpty) {
                "system"
            } else {
                appLocales.getFirstMatch(arrayOf(currentSavedLanguage))?.language ?: "system"
            }

            userSettings.setLanguage(newLanguage, withUpdate = false)
        }
    }

    @Composable
    fun MainScreen() {
        KoinContext {
            val userSettings: UserSettings = koinInject()
            val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()
            val theme by userSettings.themeFlow.collectAsStateWithLifecycle()

            val isLongPollInBackground by userSettings.longPollBackground.collectAsStateWithLifecycle()
            toggleLongPollService(true, isLongPollInBackground)

            val isOnline by userSettings.online.collectAsStateWithLifecycle()
            toggleOnlineService(isOnline)

            val language by userSettings.language.collectAsStateWithLifecycle()
            val isNeedToSetLanguage by userSettings.languageChangedFromApp.collectAsStateWithLifecycle()
            if (isNeedToSetLanguage) {
                userSettings.onLanguageChanged()
                setNewLanguage(language)
            }

            LocalLifecycleOwner.current.lifecycle.addObserver(
                LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            checkExternalLanguageUpdate(userSettings)
                            toggleOnlineService(isOnline)
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            toggleOnlineService(false)
                        }

                        else -> Unit
                    }
                }
            )

            AppTheme(
                useDarkTheme = theme.usingDarkStyle,
                useDynamicColors = theme.usingDynamicColors,
                useAmoledBackground = theme.usingAmoledBackground
            ) {
                Navigator(screen = HomeScreen(viewModel))

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

    private fun setNewLanguage(newLanguage: String) {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (newLanguage == "system") {
            if (!appLocales.isEmpty) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            }
        } else {
            if (!appLocales.toLanguageTags().startsWith(newLanguage)) {
                val newLocale = LocaleListCompat.forLanguageTags(newLanguage)
                AppCompatDelegate.setApplicationLocales(newLocale)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
    }
}

data class HomeScreen(private val viewModel: MainViewModel) : AndroidScreen() {

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
