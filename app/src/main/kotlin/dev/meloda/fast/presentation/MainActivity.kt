package dev.meloda.fast.presentation

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.runtime.mutableStateOf
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
import dev.meloda.fast.MainViewModel
import dev.meloda.fast.MainViewModelImpl
import dev.meloda.fast.common.AppConstants
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.model.LongPollState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.service.OnlineService
import dev.meloda.fast.service.longpolling.LongPollingService
import dev.meloda.fast.ui.model.DeviceSize
import dev.meloda.fast.ui.model.SizeConfig
import dev.meloda.fast.ui.model.ThemeConfig
import dev.meloda.fast.ui.theme.AppTheme
import dev.meloda.fast.ui.theme.LocalSizeConfig
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.isNeedToEnableDarkMode
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import dev.meloda.fast.ui.R as UiR

class MainActivity : AppCompatActivity() {

    @SuppressLint("HardwareIds", "InlinedApi")
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

                val deviceWidthDp = remember(true) {
                    context.resources.displayMetrics.widthPixels.pxToDp()
                }
                val deviceHeightDp = remember(true) {
                    context.resources.displayMetrics.heightPixels.pxToDp()
                }

                val deviceWidthSize by remember(deviceWidthDp) {
                    derivedStateOf {
                        when {
                            deviceWidthDp <= 360 -> DeviceSize.Small
                            deviceWidthDp <= 600 -> DeviceSize.Compact
                            deviceWidthDp <= 840 -> DeviceSize.Medium
                            else -> DeviceSize.Expanded
                        }
                    }
                }

                val deviceHeightSize by remember(deviceHeightDp) {
                    derivedStateOf {
                        when {
                            deviceHeightDp <= 480 -> DeviceSize.Small
                            deviceHeightDp <= 700 -> DeviceSize.Compact
                            deviceHeightDp <= 900 -> DeviceSize.Medium
                            else -> DeviceSize.Expanded
                        }
                    }
                }

                val sizeConfig by remember(deviceWidthSize, deviceHeightSize) {
                    mutableStateOf(
                        SizeConfig(
                            widthSize = deviceWidthSize,
                            heightSize = deviceHeightSize
                        )
                    )
                }

                val darkMode by userSettings.darkMode.collectAsStateWithLifecycle()
                val dynamicColors by userSettings.enableDynamicColors.collectAsStateWithLifecycle()
                val amoledDark by userSettings.enableAmoledDark.collectAsStateWithLifecycle()
                val enableBlur by userSettings.useBlur.collectAsStateWithLifecycle()
                val enableMultiline by userSettings.enableMultiline.collectAsStateWithLifecycle()

                val setDarkMode = isNeedToEnableDarkMode(darkMode = darkMode)

                val themeConfig by remember(
                    darkMode,
                    dynamicColors,
                    amoledDark,
                    enableBlur,
                    enableMultiline,
                    setDarkMode
                ) {
                    mutableStateOf(
                        ThemeConfig(
                            darkMode = setDarkMode,
                            dynamicColors = dynamicColors,
                            selectedColorScheme = 0,
                            amoledDark = amoledDark,
                            enableBlur = enableBlur,
                            enableMultiline = enableMultiline
                        )
                    )
                }

                CompositionLocalProvider(
                    LocalThemeConfig provides themeConfig,
                    LocalSizeConfig provides sizeConfig
                ) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
