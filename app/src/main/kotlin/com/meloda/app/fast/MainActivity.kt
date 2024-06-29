package com.meloda.app.fast

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.extensions.isSdkAtLeast
import com.meloda.app.fast.datastore.SettingsController
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.designsystem.AppTheme
import com.meloda.app.fast.designsystem.CheckPermission
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.designsystem.RequestPermission
import com.meloda.app.fast.model.MainScreenState
import com.meloda.app.fast.service.OnlineService
import com.meloda.app.fast.service.longpolling.LongPollingService
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import com.meloda.app.fast.designsystem.R as UiR

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannels()

        setContent {
            KoinContext {
                val userSettings: UserSettings = koinInject()

                val isLongPollInBackground by userSettings.longPollBackground.collectAsStateWithLifecycle()
                toggleLongPollService(true, isLongPollInBackground)

                val isOnline by userSettings.online.collectAsStateWithLifecycle()
                toggleOnlineService(isOnline)

//                val language by userSettings.language.collectAsStateWithLifecycle()
//                val isNeedToSetLanguage by userSettings.languageChangedFromApp.collectAsStateWithLifecycle()
//                if (isNeedToSetLanguage) {
//                    userSettings.onLanguageChanged()
//                    setNewLanguage(language)
//                }

                LocalLifecycleOwner.current.lifecycle.addObserver(
                    LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
//                                checkExternalLanguageUpdate(userSettings)
                                toggleOnlineService(isOnline)
                            }

                            Lifecycle.Event.ON_PAUSE -> {
                                toggleOnlineService(false)
                            }

                            else -> Unit
                        }
                    }
                )

                val theme by userSettings.theme.collectAsStateWithLifecycle()

                LaunchedEffect(true) {
                    userSettings.updateUsingDarkTheme()
                }

                AppTheme(
                    useDarkTheme = theme.usingDarkStyle,
                    useDynamicColors = theme.usingDynamicColors,
                    useAmoledBackground = theme.usingAmoledBackground
                ) {
                    RootNavigation()
//                    DestinationsNavHost(
//                        navGraph = MainNavGraph,
//                        modifier = Modifier.fillMaxSize()
//                    )

                }
            }
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
                ""
            } else {
                appLocales.getFirstMatch(arrayOf(currentSavedLanguage))?.language ?: ""
            }

            userSettings.setLanguage(newLanguage, withUpdate = false)
        }
    }

    private fun toggleLongPollService(
        enable: Boolean,
        asForeground: Boolean = SettingsController.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )
    ) {
        if (enable) {
            val longPollIntent = Intent(this, LongPollingService::class.java)

            if (asForeground) {
                ContextCompat.startForegroundService(this, longPollIntent)
            } else {
                startService(longPollIntent)
            }
        } else {
            stopService(Intent(this, LongPollingService::class.java))
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

        val asForeground = SettingsController.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )

        if (!asForeground) {
            toggleLongPollService(enable = false)
        }
    }

    private fun setNewLanguage(newLanguage: String) {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        if (newLanguage.isEmpty()) {
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
                    SettingsController.getBoolean(
                        SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                        SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
                    ))
        }
    }

    if (isNeedToCheckNotificationsPermission) {
        CheckPermission(
            showRationale = {
                MaterialDialog(
                    title = UiText.Resource(UiR.string.warning),
                    text = UiText.Simple("The application will not be able to work properly without permission to send notifications."),
                    confirmText = UiText.Simple("Grant"),
                    confirmAction = {
                        viewModel.onRequestNotificationsPermissionClicked(true)
                    },
                    cancelText = UiText.Resource(UiR.string.cancel),
                    cancelAction = viewModel::onNotificationsAlertNegativeClicked,
                    onDismissAction = viewModel::onNotificationsAlertNegativeClicked,
                    buttonsInvokeDismiss = false
                )
            },
            onDenied = {
                MaterialDialog(
                    title = UiText.Resource(UiR.string.warning),
                    text = UiText.Simple("The application needs permission to send notifications to update messages and other information."),
                    confirmText = UiText.Simple("Grant"),
                    confirmAction = {
                        viewModel.onRequestNotificationsPermissionClicked(false)
                    },
                    cancelText = UiText.Resource(UiR.string.cancel),
                    onDismissAction = {},
                    buttonsInvokeDismiss = false
                )
            },
            permission = permission
        )
    }
}
