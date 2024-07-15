package com.meloda.app.fast.presentation

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.core.app.NotificationManagerCompat
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
import com.meloda.app.fast.common.LongPollController
import com.meloda.app.fast.common.extensions.isSdkAtLeast
import com.meloda.app.fast.common.model.LongPollState
import com.meloda.app.fast.datastore.AppSettings
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.service.OnlineService
import com.meloda.app.fast.service.longpolling.LongPollingService
import com.meloda.app.fast.ui.model.ThemeConfig
import com.meloda.app.fast.ui.theme.AppTheme
import com.meloda.app.fast.ui.theme.LocalThemeConfig
import com.meloda.app.fast.ui.util.isNeedToEnableDarkMode
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import com.meloda.app.fast.ui.R as UiR

class MainActivity : AppCompatActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppSettings.deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

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
                val longPollController: LongPollController = koinInject()

                val longPollCurrentState by longPollController.currentState.collectAsStateWithLifecycle()
                val longPollStateToApply by longPollController.stateToApply.collectAsStateWithLifecycle()

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

                val sendOnline by userSettings.sendOnlineStatus.collectAsStateWithLifecycle()
                LifecycleResumeEffect(sendOnline) {
                    toggleOnlineService(sendOnline)

                    onPauseOrDispose {
                        toggleOnlineService(false)
                    }
                }

                val isDeviceCompact by remember(true) {
                    derivedStateOf {
                        context.resources.displayMetrics.widthPixels.pxToDp() <= 360
                    }
                }

                val themeConfig = ThemeConfig(
                    darkMode = isNeedToEnableDarkMode(userSettings.darkMode.value),
                    dynamicColors = userSettings.enableDynamicColors.value,
                    selectedColorScheme = 0,
                    amoledDark = userSettings.enableAmoledDark.value,
                    enableBlur = userSettings.useBlur.value,
                    enableMultiline = userSettings.enableMultiline.value,
                    isDeviceCompact = isDeviceCompact
                )

                CompositionLocalProvider(LocalThemeConfig provides themeConfig) {
                    AppTheme(
                        useDarkTheme = themeConfig.darkMode,
                        useDynamicColors = themeConfig.dynamicColors,
                        selectedColorScheme = themeConfig.selectedColorScheme,
                        useAmoledBackground = themeConfig.amoledDark,
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
            val noCategoryDescriptionText =
                getString(UiR.string.notification_channel_no_category_description)
            val noCategoryImportance = NotificationManagerCompat.IMPORTANCE_HIGH
            val noCategoryChannel =
                NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_UNCATEGORIZED,
                    noCategoryName,
                    noCategoryImportance
                ).apply {
                    description = noCategoryDescriptionText
                }

            val longPollName = getString(UiR.string.notification_channel_long_polling_service_name)
            val longPollDescriptionText =
                getString(UiR.string.notification_channel_long_polling_service_description)
            val longPollImportance = NotificationManagerCompat.IMPORTANCE_NONE
            val longPollChannel =
                NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_LONG_POLLING,
                    longPollName,
                    longPollImportance
                ).apply {
                    description = longPollDescriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannels(
                listOf(
                    noCategoryChannel,
                    longPollChannel
                )
            )
        }
    }

    private fun toggleLongPollService(
        enable: Boolean,
        inBackground: Boolean = AppSettings.Debug.longPollInBackground
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

        val asForeground = AppSettings.Debug.longPollInBackground

        if (!asForeground) {
            toggleLongPollService(enable = false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
    }
}
