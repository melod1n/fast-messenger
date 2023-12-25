package com.meloda.fast.screens.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.ext.ifEmpty
import com.meloda.fast.ext.isDebugSettingsShown
import com.meloda.fast.ext.isSdkAtLeast
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.setValue
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.model.SettingsItem
import com.meloda.fast.screens.settings.model.SettingsScreenState
import com.meloda.fast.screens.settings.model.SettingsShowOptions
import com.meloda.fast.test.TestCrashException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface SettingsViewModel {

    val screenState: StateFlow<SettingsScreenState>

    val isLongPollBackgroundEnabled: StateFlow<Boolean?>

    fun onLogOutAlertDismissed()

    fun onPerformCrashAlertDismissed()

    fun onPerformCrashPositiveButtonClicked()

    fun onLogOutAlertPositiveClick()

    fun onLongPollingAlertPositiveClicked()
    fun onLongPollingAlertDismissed()

    fun onSettingsItemClicked(key: String)
    fun onSettingsItemLongClicked(key: String)
    fun onSettingsItemChanged(key: String, newValue: Any?)

    fun onHapticsUsed()
    fun onNavigatedToUpdates()

    fun onNotificationsPermissionRequested()
}

class SettingsViewModelImpl(
    private val accountsDao: AccountsDao,
) : SettingsViewModel, ViewModel() {

    override val screenState = MutableStateFlow(SettingsScreenState.EMPTY)

    override val isLongPollBackgroundEnabled = MutableStateFlow<Boolean?>(null)

    init {
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
            accountsDao.insert(newAccount)

            UserConfig.clear()
        }
    }

    override fun onLongPollingAlertPositiveClicked() {
        screenState.setValue { old -> old.copy(isNeedToRequestNotificationPermission = true) }
    }

    override fun onLongPollingAlertDismissed() {
        screenState.setValue { old ->
            old.copy(
                showOptions = old.showOptions.copy(
                    showLongPollNotifications = false
                )
            )
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
                val showDebugCategory = isDebugSettingsShown()
                if (!showDebugCategory) return

                AppGlobal.preferences.edit {
                    putBoolean(SettingsKeys.KEY_SHOW_DEBUG_CATEGORY, false)
                }

                createSettings()

                screenState.setValue { old ->
                    old.copy(
                        useHaptics = HapticType.Reject,
                        showDebugOptions = false
                    )
                }
            }
        }
    }

    override fun onSettingsItemLongClicked(key: String) {
        when (key) {
            SettingsKeys.KEY_UPDATES_CHECK_UPDATES -> {
                val showDebugCategory = isDebugSettingsShown()
                if (showDebugCategory) return

                AppGlobal.preferences.edit {
                    putBoolean(SettingsKeys.KEY_SHOW_DEBUG_CATEGORY, true)
                }
                createSettings()

                screenState.setValue { old ->
                    old.copy(
                        useHaptics = HapticType.LongPress,
                        showDebugOptions = true
                    )
                }
            }
        }
    }

    override fun onSettingsItemChanged(key: String, newValue: Any?) {
        when (key) {
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                val isEnabled = (newValue as? Boolean).isTrue

                if (isEnabled) {
                    // TODO: 26/11/2023, Danil Nikolaev: implement
                    val isNotificationsPermissionGranted = false

                    if (!isNotificationsPermissionGranted) {
                        // TODO: 26/11/2023, Danil Nikolaev: implement restart
                    }
                }
            }
        }
    }

    override fun onHapticsUsed() {
        screenState.setValue { old -> old.copy(useHaptics = HapticType.None) }
    }

    override fun onNavigatedToUpdates() {
        screenState.setValue { old -> old.copy(isNeedToOpenUpdates = false) }
    }

    override fun onNotificationsPermissionRequested() {
        screenState.setValue { old -> old.copy(isNeedToRequestNotificationPermission = false) }
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
            val darkThemeValuesMap = darkThemeValues.mapIndexed { index, value ->
                value to darkThemeTitles[index]
            }.toMap()

            val appearanceDarkTheme = SettingsItem.ListItem.build(
                key = SettingsKeys.KEY_APPEARANCE_DARK_THEME,
                title = UiText.Resource(R.string.settings_dark_theme),
                values = darkThemeValues,
                valueTitles = darkThemeTitles,
                defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
            ) {
                summaryProvider = SettingsItem.SummaryProvider { item ->
                    val darkThemeValue = darkThemeValuesMap[item.value ?: 0]

                    UiText.ResourceParams(
                        value = R.string.settings_dark_theme_current_value,
                        args = listOf(
                            darkThemeValue
                                ?: UiText.Resource(R.string.settings_dark_theme_current_value_unknown)
                        )
                    )
                }
            }
            val appearanceUseAmoledDarkTheme = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
                defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME,
                title = UiText.Resource(R.string.settings_amoled_dark_theme),
                summary = UiText.Resource(R.string.settings_amoled_dark_theme_description)
            )
            val appearanceUseDynamicColors = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_USE_DYNAMIC_COLORS,
                title = UiText.Resource(R.string.settings_dynamic_colors),
                isVisible = isSdkAtLeast(Build.VERSION_CODES.S),
                summary = UiText.Resource(R.string.settings_dynamic_colors_description),
                defaultValue = SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
            )

            val languageValues = listOf(
                "system", "en", "ru",
            )
            val languages = listOf(
                UiText.Resource(R.string.language_system),
                UiText.Resource(R.string.language_english),
                UiText.Resource(R.string.language_russian),
            )
            val languageValuesMap = languageValues.mapIndexed { index, value ->
                value to languages[index]
            }.toMap()

            val appearanceLanguage = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_APPEARANCE_LANGUAGE,
                title = UiText.Resource(R.string.settings_application_language),
            ) {
                summaryProvider = SettingsItem.SummaryProvider { item ->
                    // TODO: 25/12/2023, Danil Nikolaev: update value, receive result from LanguagePickerScreen
                    val languageValue = languageValuesMap[item.value ?: "system"]

                    UiText.ResourceParams(
                        value = R.string.settings_application_language_value,
                        args = listOf(languageValue)
                    )
                }
            }

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
                defaultValue = SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT
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
                summary = UiText.Simple(
                    "Your messages will be updates even when app is not on the screen.\nApp will be restarted"
                )
            )

            val visibilityTitle = SettingsItem.Title.build(
                key = "visibility",
                title = UiText.Simple("Visibility")
            )
            val visibilitySendOnlineStatus = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
                defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS,
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
                title = UiText.Simple("Check updates")
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
                appearanceUseAmoledDarkTheme,
                appearanceUseDynamicColors,
                appearanceLanguage
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
                debugList,
            ).forEach(settingsList::addAll)

            if (!isDebugSettingsShown()) {
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
