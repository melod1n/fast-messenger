package com.meloda.fast.screens.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.screens.main.navigation.HomeScreen
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
            val theme by userSettings.theme.collectAsStateWithLifecycle()

            LaunchedEffect(true) {
                userSettings.updateUsingDarkTheme()
            }

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
            }
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
