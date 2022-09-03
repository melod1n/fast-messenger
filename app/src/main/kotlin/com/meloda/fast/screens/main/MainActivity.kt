package com.meloda.fast.screens.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.size
import androidx.datastore.preferences.core.edit
import androidx.drawerlayout.widget.DrawerLayout
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
import com.meloda.fast.common.*
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.databinding.ActivityMainBinding
import com.meloda.fast.extensions.gone
import com.meloda.fast.extensions.toggleVisibility
import com.meloda.fast.screens.settings.SettingsPrefsFragment
import com.meloda.fast.service.LongPollService
import com.meloda.fast.service.OnlineService
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class MainActivity : BaseActivity(R.layout.activity_main) {

    private val navigator = object : AppNavigator(this, R.id.root_fragment_container) {
        override fun setupFragmentTransaction(
            screen: FragmentScreen,
            fragmentTransaction: FragmentTransaction,
            currentFragment: Fragment?,
            nextFragment: Fragment
        ) {
        }
    }

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    lateinit var accountsDao: AccountsDao

    @Inject
    lateinit var updatesParser: LongPollUpdatesParser

    val binding: ActivityMainBinding by viewBinding()

    var useNavDrawer: Boolean by Delegates.observable(false) { _, _, _ ->
        syncNavigationMode()
    }

    override fun onResumeFragments() {
        navigatorHolder.setNavigator(navigator)
        super.onResumeFragments()
    }

    override fun onPause() {
        toggleOnlineService(false)
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        createNotificationChannels()

        AppCenter.configure(application, BuildConfig.msAppCenterAppToken)

        if (!BuildConfig.DEBUG) {
            AppCenter.start(Analytics::class.java)
        }

        AppCenter.start(Crashes::class.java)
        Crashes.setEnabled(
            AppGlobal.preferences.getBoolean(SettingsPrefsFragment.PrefEnableReporter, true)
        )

        binding.navigationBar.gone()

        lifecycleScope.launch {
            dataStore.data.map { data ->
                useNavDrawer = data[AppSettings.keyUseNavigationDrawer] ?: false
            }.collect()
        }

        if (UserConfig.currentUserId == -1) {
            openMainScreen()
        } else {
            initUserConfig()
        }

        updateManager.checkUpdates { item, _ ->
            if (item != null) {
                router.navigateTo(Screens.Updates(item))
            }
        }

        binding.drawer.getHeaderView(0).setOnLongClickListener {
            lifecycleScope.launch {
                dataStore.edit { settings ->
                    val useNavDrawer = settings[AppSettings.keyUseNavigationDrawer] ?: false
                    settings[AppSettings.keyUseNavigationDrawer] = !useNavDrawer

                    finish()
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                }
            }
            true
        }

        syncNavigationMode()
        binding.navigationBar.selectedItemId = R.id.messages

        supportFragmentManager.setFragmentResultListener(
            MainFragment.KeyStartServices,
            this
        ) { _, result ->
            val enable = result.getBoolean("enable", true)
            if (enable) {
                startServices()
            } else {
                stopServices()
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dialogsName = "Dialogs"
            val dialogsDescriptionText = "Channel for dialogs notifications"
            val dialogsImportance = NotificationManager.IMPORTANCE_MAX
            val dialogsChannel =
                NotificationChannel("simple_notifications", dialogsName, dialogsImportance).apply {
                    description = dialogsDescriptionText
                }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(dialogsChannel)
        }
    }

    override fun onResume() {
        super.onResume()

        toggleOnlineService(true)

        Crashes.getLastSessionCrashReport().thenAccept { report ->
            if (report != null) {
                if (AppGlobal.preferences.getBoolean(
                        SettingsPrefsFragment.PrefShowCrashAlert,
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
                    SettingsPrefsFragment.PrefShowDestroyedLongPollAlert,
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
        startService(Intent(this, LongPollService::class.java))
        toggleOnlineService(true)
    }

    private fun stopServices() {
        stopService(Intent(this, LongPollService::class.java))
        toggleOnlineService(false)
    }

    private fun toggleOnlineService(enable: Boolean) {
        if (enable) {
            startService(Intent(this, OnlineService::class.java))
        } else {
            stopService(Intent(this, OnlineService::class.java))
        }
    }

    private fun addTestMenuItem() {
        val test = binding.navigationBar.menu.add("Test")
        test.setIcon(R.drawable.ic_round_settings_24)
        test.setOnMenuItemClickListener {
            if (binding.navigationBar.menu.size < 5) {
                addClearMenuItem()
            } else {
                binding.navigationBar.menu.clear()
                addTestMenuItem()
            }

            true
        }
    }

    private fun addClearMenuItem() {
        binding.navigationBar.menu.add("Test").run {
            setIcon(R.drawable.ic_round_settings_24)
            setOnMenuItemClickListener {
                binding.navigationBar.menu.clear()
                addTestMenuItem()
                true
            }
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

    private fun syncNavigationMode() {
//        binding.navigationBar.toggleVisibility(!useNavDrawer)
        binding.drawerLayout.setDrawerLockMode(
            if (useNavDrawer) DrawerLayout.LOCK_MODE_UNLOCKED
            else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )
    }

    fun toggleNavBarVisibility(isVisible: Boolean, smooth: Boolean = false) {
        if (true) {
            binding.navigationBar.gone()
            return
        }

        if (useNavDrawer) {
            binding.navigationBar.gone()
        } else {
            if (smooth) {
                binding.navigationBar.toggleVisibility(isVisible)
            } else {
                binding.navigationBar.toggleVisibility(isVisible)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServices()
        updatesParser.clearListeners()
    }
}