package com.meloda.fast.screens.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.common.UpdateManagerImpl
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.ext.edgeToEdge
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.sdk26AndUp
import com.meloda.fast.screens.main.LongPollUtils.requestNotificationsPermission
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.service.LongPollService
import com.meloda.fast.service.OnlineService
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity(R.layout.activity_main) {

    private val navigator = object : AppNavigator(this, R.id.root_fragment_container) {
        override fun setupFragmentTransaction(
            screen: FragmentScreen,
            fragmentTransaction: FragmentTransaction,
            currentFragment: Fragment?,
            nextFragment: Fragment,
        ) {
        }
    }

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var updateManager: UpdateManagerImpl

    @Inject
    lateinit var accountsDao: AccountsDao

    @Inject
    lateinit var updatesParser: LongPollUpdatesParser

    private var isOnlineServiceWasLaunched: Boolean = false

    val longPollState = MutableStateFlow<LongPollState>(LongPollState.Stop)

    override fun onResumeFragments() {
        navigatorHolder.setNavigator(navigator)
        super.onResumeFragments()
    }

    override fun onPause() {
        if (isOnlineServiceWasLaunched) {
            toggleOnlineService(false)
        }
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val testTheme =
            AppGlobal.preferences.getBoolean(SettingsFragment.KEY_DEBUG_TEST_THEME, false)
        setTheme(if (testTheme) R.style.TestTheme else R.style.AppTheme)

        super.onCreate(savedInstanceState)
        edgeToEdge()

        createNotificationChannels()

        AppCenter.configure(application, BuildConfig.msAppCenterAppToken)

        if (!BuildConfig.DEBUG) {
            AppCenter.start(Analytics::class.java)
        }

        AppCenter.start(Crashes::class.java)
        Crashes.setEnabled(
            AppGlobal.preferences.getBoolean(SettingsFragment.KEY_MS_APPCENTER_ENABLE, true)
        )

        if (UserConfig.currentUserId == -1) {
            openMainScreen()
        } else {
            initUserConfig()
        }

        if (AppGlobal.preferences.getBoolean(SettingsFragment.KEY_UPDATES_CHECK_AT_STARTUP, true)) {
            var listener: Job? = null
            listener = updateManager.stateFlow.listenValue { state ->
                if (state.updateItem != null) {
                    listener?.cancel()
                    listener = null
                }
            }

            updateManager.checkUpdates()
        }

        supportFragmentManager.setFragmentResultListener(
            MainFragment.START_SERVICES_KEY,
            this
        ) { _, result ->
            val enable = result.getBoolean(MainFragment.START_SERVICES_ARG_ENABLE, true)
            if (enable) {
                requestNotificationsPermission(
                    fragmentActivity = this,
                    onStateChangedAction = { state ->
                        lifecycleScope.launch { longPollState.emit(state) }
                    }
                )

                startServices()
            } else {
                stopServices()
            }
        }

        longPollState.listenValue { state ->
            stopLongPollService()

            when (state) {
                LongPollState.DefaultService -> startLongPollService(false)
                LongPollState.ForegroundService -> startLongPollService(true)
                else -> Unit
            }
        }
    }

    private fun createNotificationChannels() {
        sdk26AndUp {
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

        if (isOnlineServiceWasLaunched) {
            toggleOnlineService(true)
        }

        Crashes.getLastSessionCrashReport().thenAccept { report ->
            if (report != null) {
                if (AppGlobal.preferences.getBoolean(
                        SettingsFragment.KEY_DEBUG_SHOW_CRASH_ALERT,
                        true
                    )
                ) {
                    val stackTrace = report.stackTrace

                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.app_crash_occurred)
                        .setMessage("Stacktrace: $stackTrace")
                        .setPositiveButton(R.string.ok, null)
                        .setNegativeButton(R.string.copy) { _, _ ->
                            AppGlobal.clipboardManager.setPrimaryClip(
                                ClipData.newPlainText(
                                    "Fast_Crash_Report",
                                    stackTrace
                                )
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

            if (AppGlobal.preferences.getBoolean(
                    SettingsFragment.KEY_DEBUG_SHOW_DESTROYED_LONG_POLL_ALERT,
                    false
                )
            ) {
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.warning)
                    .setMessage("Long poll was destroyed.")
                    .setPositiveButton("Restart this shit") { _, _ ->
                        startServices()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                startServices()
            }
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
            isOnlineServiceWasLaunched = true
            startService(Intent(this, OnlineService::class.java))
        } else {
            stopService(Intent(this, OnlineService::class.java))
        }
    }

    private fun initUserConfig() {
        if (UserConfig.currentUserId == -1) return

        lifecycleScope.launch {
            val accounts = accountsDao.getAll()

            Log.d("MainActivity", "initUserConfig: accounts: $accounts")
            if (accounts.isNotEmpty()) {
                val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                if (currentAccount != null) {
                    UserConfig.parse(currentAccount)
                }

                openMainScreen()
            } else {
                openMainScreen()
            }
        }
    }

    private fun openMainScreen() {
        router.newRootScreen(Screens.Main())
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
        updatesParser.clearListeners()
        isOnlineServiceWasLaunched = false
    }
}
