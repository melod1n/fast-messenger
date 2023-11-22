package com.meloda.fast.screens.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.database.CacheDatabase
import com.meloda.fast.ext.ifEmpty
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.setValue
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.screens.main.model.LongPollState
import com.meloda.fast.screens.settings.model.SettingsItem
import com.meloda.fast.screens.settings.model.SettingsScreenState
import com.meloda.fast.screens.settings.model.SettingsShowOptions
import com.microsoft.appcenter.crashes.model.TestCrashException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface SettingsViewModel {

    val screenState: StateFlow<SettingsScreenState>

    val isLongPollBackgroundEnabled: StateFlow<Boolean?>

    fun onLogOutAlertDismissed()

    fun onPerformCrashAlertDismissed()

    fun onPerformCrashPositiveButtonClicked()

    fun onLogOutAlertPositiveClick()

    fun onSettingsItemClicked(key: String)
    fun onSettingsItemLongClicked(key: String)
    fun onSettingsItemChanged(key: String, newValue: Any?)

    fun onHapticsUsed()
    fun onNavigatedToUpdates()
}

class SettingsViewModelImpl constructor(
    private val accountsDao: AccountsDao,
    private val cacheDatabase: CacheDatabase,
) : SettingsViewModel, ViewModel() {

    override val screenState = MutableStateFlow(SettingsScreenState.EMPTY)

    override val isLongPollBackgroundEnabled = MutableStateFlow<Boolean?>(null)

    init {
        val multilineEnabled = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_APPEARANCE_MULTILINE,
            SettingsKeys.DEFAULT_VALUE_MULTILINE
        )
        val useDynamicColors = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_USE_DYNAMIC_COLORS,
            SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )

        screenState.setValue { old ->
            old.copy(
                multilineEnabled = multilineEnabled,
                useDynamicColors = useDynamicColors
            )
        }

        createSettings()
    }

    override fun onLogOutAlertDismissed() {
        emitShowOptions { old -> old.copy(showLogOut = false) }
    }

    override fun onPerformCrashAlertDismissed() {
        emitShowOptions { old -> old.copy(showPerformCrash = false) }
    }

    override fun onPerformCrashPositiveButtonClicked() {
        throw TestCrashException()
    }

    override fun onLogOutAlertPositiveClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val newAccount = UserConfig.getAccount().copy(accessToken = "")
//            accountsDao.deleteById(UserConfig.userId)
            accountsDao.insert(newAccount)
            cacheDatabase.clearAllTables()

            MainActivity.longPollState.emit(LongPollState.Stop)

            UserConfig.clear()
        }
    }

    override fun onSettingsItemClicked(key: String) {
        when (key) {
            SettingsKeys.KEY_ACCOUNT_LOGOUT -> {
                emitShowOptions { old -> old.copy(showLogOut = true) }
            }

            SettingsKeys.KEY_UPDATES_CHECK_UPDATES -> {
                openUpdatesScreen()
            }

            SettingsKeys.KEY_DEBUG_PERFORM_CRASH -> {
                emitShowOptions { old -> old.copy(showPerformCrash = true) }
            }

            SettingsKeys.KEY_DEBUG_HIDE_DEBUG_LIST -> {
                val showDebugCategory =
                    AppGlobal.preferences.getBoolean(
                        SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
                        false
                    )
                if (!showDebugCategory) return

                AppGlobal.preferences.edit {
                    putBoolean(SettingsKeys.KEY_SHOW_DEBUG_CATEGORY, false)
                }

                createSettings()

                screenState.setValue { old -> old.copy(useHaptics = HapticType.Reject) }
            }
        }
    }

    override fun onSettingsItemLongClicked(key: String) {
        when (key) {
            SettingsKeys.KEY_UPDATES_CHECK_UPDATES -> {
                val showDebugCategory =
                    AppGlobal.preferences.getBoolean(
                        SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
                        false
                    )
                if (showDebugCategory) return

                AppGlobal.preferences.edit {
                    putBoolean(SettingsKeys.KEY_SHOW_DEBUG_CATEGORY, true)
                }
                createSettings()

                screenState.setValue { old -> old.copy(useHaptics = HapticType.LongPress) }
            }
        }
    }

    override fun onSettingsItemChanged(key: String, newValue: Any?) {
        when (key) {
            SettingsKeys.KEY_APPEARANCE_MULTILINE -> {
                val isEnabled = (newValue as? Boolean).isTrue
                screenState.setValue { old -> old.copy(multilineEnabled = isEnabled) }
            }

            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                val isEnabled = (newValue as? Boolean).isTrue
                isLongPollBackgroundEnabled.update { isEnabled }
            }
        }
    }

    override fun onHapticsUsed() {
        screenState.setValue { old -> old.copy(useHaptics = HapticType.None) }
    }

    override fun onNavigatedToUpdates() {
        screenState.setValue { old -> old.copy(isNeedToOpenUpdates = false) }
    }

    private fun emitShowOptions(function: (SettingsShowOptions) -> SettingsShowOptions) {
        val newShowOptions = function.invoke(screenState.value.showOptions)
        screenState.setValue { old -> old.copy(showOptions = newShowOptions) }
    }

    private fun createSettings() {
        viewModelScope.launch {
            val accountVisible = UserConfig.isLoggedIn()
            val accountTitle = SettingsItem.Title.build(
                key = SettingsKeys.KEY_ACCOUNT,
                title = UiText.Simple("Account")
            ) {
                isVisible = accountVisible
            }
            val accountLogOut = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_ACCOUNT_LOGOUT,
                title = UiText.Simple("Log out"),
                summary = UiText.Simple("Log out from account and delete all local data related to this account")
            ) {
                isVisible = accountVisible
            }

            val appearanceTitle = SettingsItem.Title.build(
                key = SettingsKeys.KEY_APPEARANCE,
                title = UiText.Simple("Appearance")
            )
            val appearanceMultiline = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_APPEARANCE_MULTILINE,
                defaultValue = SettingsKeys.DEFAULT_VALUE_MULTILINE,
                title = UiText.Simple("Multiline titles and messages"),
                summary = UiText.Simple("The title of the dialog and the text of the message can take up two lines")
            )

            val darkThemeValues = listOf(
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
                AppCompatDelegate.MODE_NIGHT_NO
            )
            val darkThemeTitles = listOf(
                UiText.Resource(R.string.settings_dark_theme_value_enabled),
                UiText.Resource(R.string.settings_dark_theme_value_follow_system),
                UiText.Resource(R.string.settings_dark_theme_value_battery_saver),
                UiText.Resource(R.string.settings_dark_theme_value_disabled)
            )
            val darkThemeValuesMap = List(darkThemeValues.size) { index ->
                darkThemeValues[index] to darkThemeTitles[index].parseString(AppGlobal.Instance)
            }.toMap()

            val appearanceDarkTheme = SettingsItem.ListItem.build(
                key = SettingsKeys.KEY_APPEARANCE_DARK_THEME,
                title = UiText.Resource(R.string.settings_dark_theme),
                values = darkThemeValues,
                valueTitles = darkThemeTitles,
                defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
            ) {
                summaryProvider = SettingsItem.SummaryProvider { item ->
                    val darkThemeValue =
                        darkThemeValuesMap.getOrElse(item.value ?: -1) {
                            UiText.Resource(R.string.settings_dark_theme_current_value_unknown)
                        }

                    UiText.ResourceParams(
                        value = R.string.settings_dark_theme_current_value,
                        args = listOf(darkThemeValue)
                    )
                }
            }
            val appearanceUseDynamicColors = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_USE_DYNAMIC_COLORS,
                title = UiText.Resource(R.string.settings_dynamic_colors),
                isVisible = isSdkAtLeast(Build.VERSION_CODES.S),
                summary = UiText.Resource(R.string.settings_dynamic_colors_description),
                defaultValue = SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
            )

            val featuresTitle = SettingsItem.Title.build(
                key = "features",
                title = UiText.Simple("Features")
            )
            val featuresHideKeyboardOnScroll = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL,
                defaultValue = true,
                title = UiText.Simple("Hide keyboard on scroll"),
                summary = UiText.Simple("Hides keyboard when you scrolling messages up in messages history screen")
            )
            val featuresFastText = SettingsItem.TextField.build(
                key = SettingsKeys.KEY_FEATURES_FAST_TEXT,
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
                key = SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                defaultValue = SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND,
                title = UiText.Simple("LongPoll in background"),
                summary = UiText.Simple("Your messages will be updates even when app is not on the screen")
            )

            val visibilityTitle = SettingsItem.Title.build(
                key = "visibility",
                title = UiText.Simple("Visibility")
            )
            val visibilitySendOnlineStatus = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
                defaultValue = false,
                title = UiText.Simple("Send online status"),
                summary = UiText.Simple("Online status will be sent every five minutes")
            )

            val updatesTitle = SettingsItem.Title.build(
                key = "updates",
                title = UiText.Simple("Updates")
            )
            val updatesCheckAtStartup = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_UPDATES_CHECK_AT_STARTUP,
                title = UiText.Simple("Check at startup"),
                summary = UiText.Simple("Check updates at app startup"),
                defaultValue = true
            )
            val updatesCheckUpdates = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_UPDATES_CHECK_UPDATES,
                title = UiText.Simple("Check updates"),
                isEnabled = false
            )

            val msAppCenterTitle = SettingsItem.Title.build(
                key = "msappcenter",
                title = UiText.Simple("MS AppCenter Crash Reporter")
            )
            val msAppCenterEnable = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_MS_APPCENTER_ENABLE,
                defaultValue = true,
                title = UiText.Simple("Enable Crash Reporter")
            )
            val msAppCenterEnableOnDebug = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_MS_APPCENTER_ENABLE_ON_DEBUG,
                defaultValue = false,
                title = UiText.Simple("Enable Crash Reporter on debug builds"),
                summary = UiText.Simple("Requires application restart")
            )

            val debugTitle = SettingsItem.Title.build(
                key = "debug",
                title = UiText.Simple("Debug")
            )
            val debugPerformCrash = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_DEBUG_PERFORM_CRASH,
                title = UiText.Simple("Perform crash"),
                summary = UiText.Simple("App will be crashed. Obviously")
            )
            val debugShowCrashAlert = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_DEBUG_SHOW_CRASH_ALERT,
                defaultValue = true,
                title = UiText.Simple("Show alert after crash"),
                summary = UiText.Simple("Shows alert dialog with stacktrace after app crashed\n(it will be not shown if you perform crash manually)")
            )
            val debugShowExactTimeOnTimeStamp = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_SHOW_EXACT_TIME_ON_TIME_STAMP,
                title = UiText.Simple("[WIP] Show exact time on time stamp"),
                summary = UiText.Simple("Shows hours and minutes on time stamp in messages history"),
                defaultValue = false
            )

            val debugHideDebugList = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_DEBUG_HIDE_DEBUG_LIST,
                title = UiText.Simple("Hide debug list")
            )

            val accountList = listOf(
                accountTitle,
                accountLogOut
            )
            val appearanceList = listOf(
                appearanceTitle,
                appearanceMultiline,
                appearanceDarkTheme,
                appearanceUseDynamicColors
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
            val msAppCenterList = mutableListOf(
                msAppCenterTitle,
                msAppCenterEnable,
            ).apply {
                if (BuildConfig.DEBUG) {
                    this += msAppCenterEnableOnDebug
                }
            }
            val debugList = mutableListOf<SettingsItem<*>>()
            listOf(
                debugTitle,
                debugPerformCrash,
                debugShowCrashAlert,
                debugShowExactTimeOnTimeStamp
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
                    SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
                    false
                )
            ) {
                settingsList.removeAll(debugList)
            }

            screenState.setValue { old -> old.copy(settings = settingsList) }
        }
    }

    private fun openUpdatesScreen() {
        screenState.setValue { old -> old.copy(isNeedToOpenUpdates = true) }
    }
}

sealed interface HapticType {
    data object LongPress : HapticType
    data object Reject : HapticType
    data object None : HapticType

    fun getHaptic(): Int {
        return when (this) {
            LongPress -> HapticFeedbackConstantsCompat.LONG_PRESS
            Reject -> HapticFeedbackConstantsCompat.REJECT
            None -> -1
        }
    }
}
