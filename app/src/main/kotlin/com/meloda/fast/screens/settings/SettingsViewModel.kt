package com.meloda.fast.screens.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.database.CacheDatabase
import com.meloda.fast.ext.emitOnMainScope
import com.meloda.fast.ext.ifEmpty
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.ext.isTrue
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString
import com.meloda.fast.screens.main.activity.LongPollState
import com.meloda.fast.screens.main.activity.MainActivity
import com.meloda.fast.screens.settings.model.SettingsItem
import com.microsoft.appcenter.crashes.model.TestCrashException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias SettingsList = List<SettingsItem<*>>

interface SettingsViewModel {

    val settings: StateFlow<SettingsList>
    val useDynamicColors: StateFlow<Boolean>
    val useLargeTopAppBar: StateFlow<Boolean>
    val isMultilineEnabled: StateFlow<Boolean>
    val isLongPollBackgroundEnabled: StateFlow<Boolean?>

    val isNeedToShowLogOutAlert: StateFlow<Boolean>

    val isNeedToOpenTestingActivity: StateFlow<Boolean>

    fun onLogOutAlertDismissed()

    fun onLogOutAlertPositiveClick()

    fun onSettingsItemClicked(key: String)
    fun onSettingsItemLongClicked(key: String): Boolean
    fun onSettingsItemChanged(key: String, newValue: Any?)

    fun onTestingActivityOpened()
}

class SettingsViewModelImpl constructor(
    private val accountsDao: AccountsDao,
    private val cacheDatabase: CacheDatabase,
    private val router: Router
) : SettingsViewModel, ViewModel() {

    override val settings = MutableStateFlow<SettingsList>(emptyList())
    override val useDynamicColors = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_USE_DYNAMIC_COLORS,
            SettingsFragment.DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )
    )
    override val useLargeTopAppBar = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_USE_LARGE_TOP_APP_BAR,
            SettingsFragment.DEFAULT_VALUE_USE_LARGE_TOP_APP_BAR
        )
    )
    override val isMultilineEnabled = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_APPEARANCE_MULTILINE,
            SettingsFragment.DEFAULT_VALUE_MULTILINE
        )
    )
    override val isLongPollBackgroundEnabled = MutableStateFlow<Boolean?>(null)

    override val isNeedToShowLogOutAlert = MutableStateFlow(false)

    override val isNeedToOpenTestingActivity = MutableStateFlow(false)

    init {
        createSettings()
    }

    private fun createSettings() {
        viewModelScope.launch {
            val accountVisible = UserConfig.isLoggedIn()
            val accountTitle = SettingsItem.Title.build(
                key = SettingsFragment.KEY_ACCOUNT,
                title = UiText.Simple("Account")
            ) {
                isVisible = accountVisible
            }
            val accountLogOut = SettingsItem.TitleSummary.build(
                key = SettingsFragment.KEY_ACCOUNT_LOGOUT,
                title = UiText.Simple("Log out"),
                summary = UiText.Simple("Log out from account and delete all local data related to this account")
            ) {
                isVisible = accountVisible
            }

            val appearanceTitle = SettingsItem.Title.build(
                key = SettingsFragment.KEY_APPEARANCE,
                title = UiText.Simple("Appearance")
            )
            val appearanceMultiline = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_APPEARANCE_MULTILINE,
                defaultValue = SettingsFragment.DEFAULT_VALUE_MULTILINE,
                title = UiText.Simple("Multiline titles and messages"),
                summary = UiText.Simple("The title of the dialog and the text of the message can take up two lines")
            )

            val featuresTitle = SettingsItem.Title.build(
                key = "features",
                title = UiText.Simple("Features")
            )
            val featuresHideKeyboardOnScroll = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL,
                defaultValue = true,
                title = UiText.Simple("Hide keyboard on scroll")
            )
            val featuresFastText = SettingsItem.TextField.build(
                key = SettingsFragment.KEY_FEATURES_FAST_TEXT,
                title = UiText.Simple("Fast text"),
                defaultValue = "¯\\_(ツ)_/¯",
            ).apply {
                summaryProvider = SettingsItem.SummaryProvider { settingsItem ->
                    UiText.ResourceParams(
                        R.string.pref_message_fast_text_summary,
                        listOf(settingsItem.value.ifEmpty { null })
                    )
                }
            }
            val featuresLongPollBackground = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                defaultValue = SettingsFragment.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND,
                title = UiText.Simple("LongPoll in background"),
                summary = UiText.Simple("Your messages will be updates even when app is not on the screen")
            )

            val visibilityTitle = SettingsItem.Title.build(
                key = "visibility",
                title = UiText.Simple("Visibility")
            )
            val visibilitySendOnlineStatus = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_VISIBILITY_SEND_ONLINE_STATUS,
                defaultValue = false,
                title = UiText.Simple("Send online status"),
                summary = UiText.Simple("Online status will be sent every five minutes")
            )

            val updatesTitle = SettingsItem.Title.build(
                key = "updates",
                title = UiText.Simple("Updates")
            )
            val updatesCheckAtStartup = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_UPDATES_CHECK_AT_STARTUP,
                title = UiText.Simple("Check at startup"),
                summary = UiText.Simple("Check updates at app startup"),
                defaultValue = true
            )
            val updatesCheckUpdates = SettingsItem.TitleSummary.build(
                key = SettingsFragment.KEY_UPDATES_CHECK_UPDATES,
                title = UiText.Simple("Check updates")
            )

            val msAppCenterTitle = SettingsItem.Title.build(
                key = "msappcenter",
                title = UiText.Simple("MS AppCenter Crash Reporter")
            )
            val msAppCenterEnable = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_MS_APPCENTER_ENABLE,
                defaultValue = true,
                title = UiText.Simple("Enable Crash Reporter")
            )

            val debugTitle = SettingsItem.Title.build(
                key = "debug",
                title = UiText.Simple("Debug")
            )
            val debugPerformCrash = SettingsItem.TitleSummary.build(
                key = SettingsFragment.KEY_DEBUG_PERFORM_CRASH,
                title = UiText.Simple("Perform crash"),
                summary = UiText.Simple("App will be crashed. Obviously")
            )
            val debugShowCrashAlert = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_DEBUG_SHOW_CRASH_ALERT,
                defaultValue = true,
                title = UiText.Simple("Show alert after crash"),
                summary = UiText.Simple("Shows alert dialog with stacktrace after app crashed\n(it will be not shown if you perform crash manually)")
            )
            val debugUseDynamicColors = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_USE_DYNAMIC_COLORS,
                title = UiText.Simple("[WIP] Use dynamic colors"),
                isEnabled = isSdkAtLeast(Build.VERSION_CODES.S),
                summary = UiText.Simple("Requires Android 12 or higher;\nUnstable - you may need to manually kill app via it's info screen in order for changes to applied"),
                defaultValue = false
            )

            val darkThemeValues = listOf(
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            )
            val darkThemeTitles = listOf(
                UiText.Simple("Enabled"),
                UiText.Simple("Disabled"),
                UiText.Simple("Follow system"),
                UiText.Simple("Battery saver")
            )
            val darkThemeValuesMap = List(darkThemeValues.size) { index ->
                darkThemeValues[index] to darkThemeTitles[index].parseString(AppGlobal.Instance)
            }.toMap()

            val debugDarkTheme = SettingsItem.ListItem.build(
                key = SettingsFragment.KEY_APPEARANCE_DARK_THEME,
                title = UiText.Simple("[WIP] Dark theme"),
                values = darkThemeValues,
                valueTitles = darkThemeTitles,
                defaultValue = AppCompatDelegate.MODE_NIGHT_NO
            ) {
                summaryProvider = SettingsItem.SummaryProvider { item ->
                    UiText.Simple(
                        "Current value: ${
                            darkThemeValuesMap.getOrElse(item.value ?: -1) {
                                "Unknown"
                            }
                        }"
                    )
                }
            }
            val debugUseLargeTopAppBar = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_USE_LARGE_TOP_APP_BAR,
                title = UiText.Simple("[WIP] Use LargeTopAppBar"),
                summary = UiText.Simple("Only in settings screen"),
                defaultValue = SettingsFragment.DEFAULT_VALUE_USE_LARGE_TOP_APP_BAR
            )
            val debugUseBlur = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_USE_BLUR,
                defaultValue = SettingsFragment.DEFAULT_VALUE_USE_BLUR,
                title = UiText.Simple("[WIP] Use blur"),
                summary = UiText.Simple("Use blur wherever it's possible")
            )
            val debugUseCompose = SettingsItem.Switch.build(
                key = SettingsFragment.KEY_USE_COMPOSE,
                defaultValue = SettingsFragment.DEFAULT_VALUE_USE_COMPOSE,
                title = UiText.Simple("Use Compose"),
                summary = UiText.Simple("Use Compose on those screens where there is a test implementation of it")
            )
            val debugOpenTestingActivity = SettingsItem.TitleSummary.build(
                key = SettingsFragment.KEY_OPEN_TESTING_ACTIVITY,
                title = UiText.Simple("Open testing activity")
            )

            val debugHideDebugList = SettingsItem.TitleSummary.build(
                key = SettingsFragment.KEY_DEBUG_HIDE_DEBUG_LIST,
                title = UiText.Simple("Hide debug list")
            )

            val accountList = listOf(
                accountTitle,
                accountLogOut
            )
            val appearanceList = listOf(
                appearanceTitle,
                appearanceMultiline
            )
            val featuresList = listOf(
                featuresTitle,
                featuresHideKeyboardOnScroll,
                featuresFastText,
                featuresLongPollBackground
            )
            val visibilityList = listOf(
                visibilityTitle,
                visibilitySendOnlineStatus,
            )
            val updatesList = listOf(
                updatesTitle,
                updatesCheckAtStartup,
                updatesCheckUpdates,
            )
            val msAppCenterList = listOf(
                msAppCenterTitle,
                msAppCenterEnable,
            )
            val debugList = mutableListOf<SettingsItem<*>>()
            listOf(
                debugTitle,
                debugPerformCrash,
                debugShowCrashAlert,
                debugUseDynamicColors,
                debugDarkTheme,
                debugUseLargeTopAppBar,
                debugUseBlur,
                debugUseCompose,
                debugOpenTestingActivity
            ).forEach(debugList::add)

            debugList += debugHideDebugList

            val settingsList = mutableListOf<SettingsItem<*>>()
            listOf(
                accountList,
                appearanceList,
                featuresList,
                visibilityList,
                updatesList,
                msAppCenterList,
                debugList,
            ).forEach(settingsList::addAll)

            if (!AppGlobal.preferences.getBoolean(
                    SettingsFragment.KEY_SHOW_DEBUG_CATEGORY,
                    false
                )
            ) {
                settingsList.removeAll(debugList)
            }

            settings.emit(settingsList)
        }
    }

    override fun onLogOutAlertDismissed() {
        viewModelScope.launch(Dispatchers.Main) {
            isNeedToShowLogOutAlert.emit(false)
        }
    }

    override fun onLogOutAlertPositiveClick() {
        viewModelScope.launch(Dispatchers.IO) {
            accountsDao.deleteById(UserConfig.userId)
            cacheDatabase.clearAllTables()

            MainActivity.longPollState.emit(LongPollState.Stop)

            UserConfig.clear()

            withContext(Dispatchers.Main) {
                router.newRootScreen(Screens.Main())
            }
        }
    }

    override fun onSettingsItemClicked(key: String) {
        when (key) {
            SettingsFragment.KEY_ACCOUNT_LOGOUT -> {
                viewModelScope.launch(Dispatchers.Main) {
                    isNeedToShowLogOutAlert.emit(true)
                }
            }

            SettingsFragment.KEY_UPDATES_CHECK_UPDATES -> {
                openUpdatesScreen()
            }

            SettingsFragment.KEY_DEBUG_PERFORM_CRASH -> {
                throw TestCrashException()
            }

            SettingsFragment.KEY_DEBUG_HIDE_DEBUG_LIST -> {
                val showDebugCategory =
                    AppGlobal.preferences.getBoolean(
                        SettingsFragment.KEY_SHOW_DEBUG_CATEGORY,
                        false
                    )
                if (!showDebugCategory) return

                AppGlobal.preferences.edit {
                    putBoolean(SettingsFragment.KEY_SHOW_DEBUG_CATEGORY, false)
                }

                createSettings()
            }

            SettingsFragment.KEY_OPEN_TESTING_ACTIVITY -> {
                isNeedToOpenTestingActivity.emitOnMainScope(true)
            }
        }
    }

    override fun onSettingsItemLongClicked(key: String): Boolean {
        return when (key) {
            SettingsFragment.KEY_UPDATES_CHECK_UPDATES -> {
                val showDebugCategory =
                    AppGlobal.preferences.getBoolean(
                        SettingsFragment.KEY_SHOW_DEBUG_CATEGORY,
                        false
                    )
                if (showDebugCategory) return false

                AppGlobal.preferences.edit {
                    putBoolean(SettingsFragment.KEY_SHOW_DEBUG_CATEGORY, true)
                }
                createSettings()
                true
            }

            else -> false
        }
    }

    override fun onSettingsItemChanged(key: String, newValue: Any?) {
        when (key) {
            SettingsFragment.KEY_APPEARANCE_DARK_THEME -> {
                val newMode = newValue as? Int ?: return
                AppCompatDelegate.setDefaultNightMode(newMode)
            }

            SettingsFragment.KEY_APPEARANCE_MULTILINE -> {
                val isEnabled = (newValue as? Boolean).isTrue
                isMultilineEnabled.update { isEnabled }
            }

            SettingsFragment.KEY_USE_DYNAMIC_COLORS -> {
                val isEnabled = (newValue as? Boolean).isTrue
                useDynamicColors.update { isEnabled }
            }

            SettingsFragment.KEY_USE_LARGE_TOP_APP_BAR -> {
                val isEnabled = (newValue as? Boolean).isTrue
                useLargeTopAppBar.update { isEnabled }
            }

            SettingsFragment.KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                val isEnabled = (newValue as? Boolean).isTrue
                isLongPollBackgroundEnabled.update { isEnabled }
            }
        }
    }

    override fun onTestingActivityOpened() {
        isNeedToOpenTestingActivity.emitOnMainScope(false)
    }

    private fun openUpdatesScreen() {
        router.navigateTo(Screens.Updates())
    }
}
