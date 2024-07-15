package com.meloda.app.fast.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.conena.nanokt.android.content.pxToDp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.meloda.app.fast.MainViewModel
import com.meloda.app.fast.MainViewModelImpl
import com.meloda.app.fast.common.AppConstants
import com.meloda.app.fast.common.extensions.isSdkAtLeast
import com.meloda.app.fast.datastore.SettingsController
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.datastore.model.LongPollState
import com.meloda.app.fast.datastore.model.ThemeConfig
import com.meloda.app.fast.designsystem.AppTheme
import com.meloda.app.fast.designsystem.LocalTheme
import com.meloda.app.fast.service.OnlineService
import com.meloda.app.fast.service.longpolling.LongPollingService
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

import com.meloda.app.fast.designsystem.R as UiR

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val systemBarStyle = when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> SystemBarStyle.light(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )

            Configuration.UI_MODE_NIGHT_YES -> SystemBarStyle.dark(Color.Transparent.toArgb())
            else -> error("Illegal State, current mode is $currentNightMode")
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )

        createNotificationChannels()

        setContent {
            KoinContext {
                val context = LocalContext.current
                val userSettings: UserSettings = koinInject()

                val longPollCurrentState by userSettings.longPollCurrentState.collectAsStateWithLifecycle()
                val longPollStateToApply by userSettings.longPollStateToApply.collectAsStateWithLifecycle()

                val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()

                LifecycleResumeEffect(true) {
                    viewModel.onAppResumed()
                    onPauseOrDispose {}
                }

                val permissionState =
                    rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

                val isNeedToCheckPermission by viewModel.isNeedToCheckNotificationsPermission.collectAsStateWithLifecycle()
                val isNeedToRequestPermission by viewModel.isNeedToRequestNotifications.collectAsStateWithLifecycle()

                LaunchedEffect(isNeedToCheckPermission) {
                    if (isNeedToCheckPermission) {
                        viewModel.onPermissionCheckStatus(permissionState.status)

                        if (permissionState.status.isGranted) {
                            if (longPollCurrentState == LongPollState.InApp) {
                                toggleLongPollService(false)
                            }

                            toggleLongPollService(
                                enable = true,
                                inBackground = true
                            )
                        }
                    }
                }

                LaunchedEffect(isNeedToRequestPermission) {
                    if (isNeedToRequestPermission) {
                        viewModel.onPermissionsRequested()
                        permissionState.launchPermissionRequest()
                    }
                }

                LaunchedEffect(longPollStateToApply) {
                    if (longPollStateToApply != LongPollState.Background) {
                        if (longPollStateToApply.isLaunched() && longPollCurrentState.isLaunched()
                            && longPollCurrentState != longPollStateToApply
                        ) {
                            toggleLongPollService(false)
                            Log.d("LongPoll", "recreate()")
                        }

                        toggleLongPollService(
                            enable = longPollStateToApply.isLaunched(),
                            inBackground = longPollStateToApply == LongPollState.Background
                        )
                    }
                }

                val isOnline by userSettings.online.collectAsStateWithLifecycle()
                LifecycleResumeEffect(isOnline) {
                    toggleOnlineService(isOnline)

                    onPauseOrDispose {
                        toggleOnlineService(false)
                    }
                }

                val isDeviceCompact by remember(true) {
                    derivedStateOf {
                        context.resources.displayMetrics.widthPixels.pxToDp() <= 360
                    }
                }

                val theme by userSettings.theme.collectAsStateWithLifecycle()
                CompositionLocalProvider(
                    LocalTheme provides ThemeConfig(
                        usingDarkStyle = theme.usingDarkStyle,
                        usingDynamicColors = theme.usingDynamicColors,
                        selectedColorScheme = theme.selectedColorScheme,
                        usingAmoledBackground = theme.usingAmoledBackground,
                        usingBlur = theme.usingBlur,
                        multiline = theme.multiline,
                        isDeviceCompact = isDeviceCompact
                    )
                ) {
                    val currentTheme = LocalTheme.current

                    AppTheme(
                        useDarkTheme = currentTheme.usingDarkStyle,
                        useDynamicColors = currentTheme.usingDynamicColors,
                        selectedColorScheme = currentTheme.selectedColorScheme,
                        useAmoledBackground = currentTheme.usingAmoledBackground,
                    ) {
                        RootScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    private fun createNotificationChannels() {
        isSdkAtLeast(Build.VERSION_CODES.O) {
            val noCategoryName = getString(UiR.string.notification_channel_no_category_name)
            val noCategoryDescriptionText = getString(UiR.string.notification_channel_no_category_description)
            val noCategoryImportance = NotificationManager.IMPORTANCE_HIGH
            val noCategoryChannel =
                NotificationChannel(AppConstants.NOTIFICATION_CHANNEL_UNCATEGORIZED, noCategoryName, noCategoryImportance).apply {
                    description = noCategoryDescriptionText
                }

            val longPollName = getString(UiR.string.notification_channel_long_polling_service_name)
            val longPollDescriptionText = getString(UiR.string.notification_channel_long_polling_service_description)
            val longPollImportance = NotificationManager.IMPORTANCE_NONE
            val longPollChannel =
                NotificationChannel(AppConstants.NOTIFICATION_CHANNEL_LONG_POLLING, longPollName, longPollImportance).apply {
                    description = longPollDescriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannels(listOf(noCategoryChannel, longPollChannel))
        }
    }

    private fun toggleLongPollService(
        enable: Boolean,
        inBackground: Boolean = SettingsController.isLongPollInBackgroundEnabled
    ) {
        if (enable) {
            val longPollIntent = Intent(this, LongPollingService::class.java)

            if (inBackground) {
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

        val asForeground = SettingsController.isLongPollInBackgroundEnabled

        if (!asForeground) {
            toggleLongPollService(enable = false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
    }
}
