package dev.meloda.fast.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dev.meloda.fast.MainViewModel
import dev.meloda.fast.MainViewModelImpl
import dev.meloda.fast.common.AppConstants
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.service.OnlineService
import dev.meloda.fast.service.longpolling.LongPollingService
import dev.meloda.fast.ui.R
import org.koin.androidx.compose.koinViewModel

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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Color.Gray.copy(alpha = 0.85f).toArgb()
                } else {
                    Color.Transparent.toArgb()
                }
            )

            Configuration.UI_MODE_NIGHT_YES -> SystemBarStyle.dark(Color.Transparent.toArgb())
            else -> error("Illegal State, current mode is $currentNightMode")
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )

        createNotificationChannels()
        requestNotificationPermissions()

        setContent {
            val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
            LaunchedEffect(viewModel) {
                Log.d("VM_CREATE", "onCreate: viewModel: $viewModel")
            }

            LifecycleResumeEffect(true) {
                viewModel.onAppResumed(intent)
                onPauseOrDispose {}
            }

            RootScreen(
                toggleLongPollService = { enable, inBackground ->
                    toggleLongPollService(
                        enable = enable,
                        inBackground = inBackground ?: AppSettings.Experimental.longPollInBackground
                    )
                },
                toggleOnlineService = ::toggleOnlineService
            )
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val noCategoryName = getString(R.string.notification_channel_no_category_name)
            val noCategoryDescriptionText =
                getString(R.string.notification_channel_no_category_description)
            val noCategoryChannel =
                NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_UNCATEGORIZED,
                    noCategoryName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = noCategoryDescriptionText
                }

            val longPollName = getString(R.string.notification_channel_long_polling_service_name)
            val longPollDescriptionText =
                getString(R.string.notification_channel_long_polling_service_description)
            val longPollChannel =
                NotificationChannel(
                    AppConstants.NOTIFICATION_CHANNEL_LONG_POLLING,
                    longPollName,
                    NotificationManager.IMPORTANCE_NONE
                ).apply {
                    description = longPollDescriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannels(
                listOf(
                    noCategoryChannel,
                    longPollChannel
                )
            )
        }
    }

    private fun requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION_CODE
            )
        }
    }

    private val longPollingServiceIntent by lazy {
        Intent(this, LongPollingService::class.java)
    }
    private val onlineServiceIntent by lazy {
        Intent(this, OnlineService::class.java)
    }

    private fun toggleLongPollService(
        enable: Boolean,
        inBackground: Boolean = AppSettings.Experimental.longPollInBackground
    ) {
        if (enable) {
            val longPollIntent = longPollingServiceIntent

            if (inBackground) {
                ContextCompat.startForegroundService(this, longPollIntent)
            } else {
                startService(longPollIntent)
            }
        } else {
            stopService(longPollingServiceIntent)
        }
    }

    private fun toggleOnlineService(enable: Boolean) {
        if (enable) {
            startService(onlineServiceIntent)
        } else {
            stopService(onlineServiceIntent)
        }
    }

    private fun stopServices() {
        toggleOnlineService(enable = false)

        val asForeground = AppSettings.Experimental.longPollInBackground

        if (!asForeground) {
            toggleLongPollService(enable = false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION_CODE = 1
    }
}
