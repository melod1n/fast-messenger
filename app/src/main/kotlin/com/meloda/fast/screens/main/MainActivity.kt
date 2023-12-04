package com.meloda.fast.screens.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.RequestNotificationsPermission
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.screens.conversations.navigation.ConversationsScreen
import com.meloda.fast.screens.login.navigation.LoginScreen
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.service.LongPollService
import com.meloda.fast.service.OnlineService
import com.meloda.fast.ui.AppTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainScreen()
        }

        startLongPollService()

        createNotificationChannels()
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

    override fun onResume() {
        super.onResume()

        if (AppGlobal.preferences.getBoolean(LongPollService.KeyLongPollWasDestroyed, false)) {
            AppGlobal.preferences.edit {
                putBoolean(LongPollService.KeyLongPollWasDestroyed, false)
            }

            startServices()
        }
    }

    private fun startServices() {
        toggleOnlineService(true)
    }

    private fun stopServices() {
        toggleOnlineService(false)
    }

    private fun startLongPollService() {
        val asForeground = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )

        val longPollIntent = Intent(this, LongPollService::class.java)

        if (asForeground) {
            ContextCompat.startForegroundService(this, longPollIntent)
        } else {
            startService(longPollIntent)
        }
    }

    private fun toggleOnlineService(enable: Boolean) {
        if (enable) {
            startService(Intent(this, OnlineService::class.java))
        } else {
            stopService(Intent(this, OnlineService::class.java))
        }
    }

    @Composable
    fun MainScreen() {
        KoinContext {
            val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
            val userSettings: UserSettings = koinInject()
            val theme by userSettings.themeFlow.collectAsStateWithLifecycle()

            val screenState by viewModel.screenState.collectAsStateWithLifecycle()
            toggleOnlineService(screenState.onlineServiceEnabled)

            AppTheme(
                useDarkTheme = theme.usingDarkStyle,
                useDynamicColors = theme.usingDynamicColors
            ) {
                Navigator(
                    screen = HomeScreen(viewModel = viewModel)
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && AppGlobal.preferences.getBoolean(
                        SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                        SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
                    )
                ) {
                    // TODO: 26/11/2023, Danil Nikolaev: implement show dialog for long polling
                    RequestNotificationsPermission {
                        Text(text = "Blya pizda")
                    }
                }
            }
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
