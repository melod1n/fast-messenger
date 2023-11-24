package com.meloda.fast.screens.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.screens.conversations.navigation.ConversationsScreen
import com.meloda.fast.screens.login.navigation.LoginScreen
import com.meloda.fast.screens.main.model.LongPollState
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.service.LongPollService
import com.meloda.fast.service.OnlineService
import com.meloda.fast.ui.AppTheme
import com.meloda.fast.util.AndroidUtils
import com.microsoft.appcenter.crashes.Crashes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject


class MainActivity : ComponentActivity() {

    private val updatesParser: LongPollUpdatesParser by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MainScreen()
        }

        createNotificationChannels()

//        AppCenter.configure(application, BuildConfig.msAppCenterAppToken)

//        if (!BuildConfig.DEBUG) {
//            AppCenter.start(Analytics::class.java)
//        }
//
//        val enableCrashLogs =
//            AppGlobal.preferences.getBoolean(SettingsKeys.KEY_MS_APPCENTER_ENABLE, true)
//                    || (BuildConfig.DEBUG && AppGlobal.preferences.getBoolean(
//                SettingsKeys.KEY_MS_APPCENTER_ENABLE_ON_DEBUG,
//                false
//            ))
//
//        if (enableCrashLogs) {
//            AppCenter.start(Crashes::class.java)
//        }


//        if (UserConfig.currentUserId == -1) {
//            openMainScreen()
//        } else {
//            initUserConfig()
//        }

        // TODO: 09.04.2023, Danil Nikolaev: implement checking updates on startup

        // TODO: 09.04.2023, Danil Nikolaev: rewrite this
//        supportFragmentManager.setFragmentResultListener(
//            MainFragment.START_SERVICES_KEY,
//            this
//        ) { _, result ->
//            val enable = result.getBoolean(MainFragment.START_SERVICES_ARG_ENABLE, true)
//            if (enable) {
//                requestNotificationsPermission(
//                    fragmentActivity = this,
//                    onStateChangedAction = { state ->
//                        lifecycleScope.launch { longPollState.emit(state) }
//                    }
//                )
//
//                startServices()
//            } else {
//                stopServices()
//            }
//        }

        // TODO: 09.04.2023, Danil Nikolaev: rewrite this
//        longPollState.listenValue { state ->
//            stopLongPollService()
//
//            when (state) {
//                LongPollState.DefaultService -> startLongPollService(false)
//                LongPollState.ForegroundService -> startLongPollService(true)
//                else -> Unit
//            }
//        }
    }

    private fun initUserConfig() {
        if (UserConfig.currentUserId == -1) return

        lifecycleScope.launch {

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

    override fun onResume() {
        super.onResume()

        Crashes.getLastSessionCrashReport().thenAccept { report ->
            if (report != null) {
                if (AppGlobal.preferences.getBoolean(
                        SettingsKeys.KEY_DEBUG_SHOW_CRASH_ALERT,
                        true
                    )
                ) {
                    val stackTrace = report.stackTrace

                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.app_crash_occurred)
                        .setMessage("Stacktrace: $stackTrace")
                        .setPositiveButton(R.string.ok, null)
                        .setNegativeButton(R.string.copy) { _, _ ->
                            AndroidUtils.copyText(
                                label = "Fast_Crash_Report",
                                text = stackTrace
                            )
                            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
                        }
                        .setNeutralButton(R.string.share) { _, _ ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, stackTrace)
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(sendIntent, "Share stacktrace")
                            try {
                                startActivity(shareIntent)
                            } catch (e: Exception) {
                                e.printStackTrace()

                                runOnUiThread {
                                    MaterialAlertDialogBuilder(this)
                                        .setTitle(R.string.warning)
                                        .setMessage("Can't share")
                                        .setPositiveButton(R.string.ok, null)
                                        .show()
                                }
                            }
                        }
                        .show()
                }
            }
        }

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

    private fun createLongPollIntent(asForeground: Boolean? = null): Intent =
        Intent(this, LongPollService::class.java).apply {
            asForeground?.let { putExtra("foreground", it) }
        }

    private fun startLongPollService(asForeground: Boolean) {
        val longPollIntent = createLongPollIntent(asForeground)

        if (asForeground) {
            ContextCompat.startForegroundService(this, longPollIntent)
        } else {
            startService(longPollIntent)
        }
    }

    private fun stopLongPollService() {
        stopService(createLongPollIntent())
    }

    private fun toggleOnlineService(enable: Boolean) {
        if (enable) {
            startService(Intent(this, OnlineService::class.java))
        } else {
            stopService(Intent(this, OnlineService::class.java))
        }
    }

    private val exitScreens = listOf(
        LoginScreen::class.java,
        ConversationsScreen::class.java
    )

    @Composable
    fun MainScreen() {
        KoinContext {
            val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
            val userSettings: UserSettings = koinInject()
            val theme by userSettings.themeFlow.collectAsStateWithLifecycle()

            AppTheme(
                useDarkTheme = theme.usingDarkStyle,
                useDynamicColors = theme.usingDynamicColors
            ) {
                Navigator(
                    screen = HomeScreen(viewModel = viewModel),
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
        updatesParser.clearListeners()
    }

    companion object {
        val longPollState = MutableStateFlow<LongPollState>(LongPollState.Stop)
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
