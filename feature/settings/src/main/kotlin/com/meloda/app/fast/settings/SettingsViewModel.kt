package com.meloda.app.fast.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.isSdkAtLeast
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.datastore.SettingsController
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.isDebugSettingsShown
import com.meloda.app.fast.model.database.AccountEntity
import com.meloda.app.fast.settings.model.SettingsItem
import com.meloda.app.fast.settings.model.SettingsScreenState
import com.meloda.app.fast.settings.model.SettingsShowOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.meloda.app.fast.designsystem.R as UiR

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

    fun onNotificationsPermissionRequested()
}

class SettingsViewModelImpl(
    private val accountsRepository: AccountsRepository,
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
        throw Exception("Test exception")
    }

    override fun onLogOutAlertPositiveClick() {
        viewModelScope.launch(Dispatchers.IO) {
            accountsRepository.storeAccounts(
                listOf(
                    AccountEntity(
                        userId = UserConfig.userId,
                        accessToken = "",
                        fastToken = UserConfig.fastToken,
                        trustedHash = UserConfig.trustedHash
                    )
                )
            )

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

            SettingsKeys.KEY_DEBUG_PERFORM_CRASH -> {
                emitShowOptions { old -> old.copy(showPerformCrash = true) }
            }

            SettingsKeys.KEY_DEBUG_HIDE_DEBUG_LIST -> {
                val showDebugCategory = isDebugSettingsShown()
                if (!showDebugCategory) return

                SettingsController.put(
                    SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
                    false
                )

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
            SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS -> {
                val showDebugCategory = isDebugSettingsShown()
                if (showDebugCategory) return

                SettingsController.put(SettingsKeys.KEY_SHOW_DEBUG_CATEGORY, true)

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
                val isEnabled = (newValue as? Boolean) == true

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

            val generalTitle = SettingsItem.Title.build(
                key = SettingsKeys.KEY_GENERAL,
                title = UiText.Simple("General")
            )
            val generalUseContactNames = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_USE_CONTACT_NAMES,
                title = UiText.Simple("Use contact names"),
                summary = UiText.Simple("App will use available contact names for users"),
                defaultValue = SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
            )

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
                AppCompatDelegate.MODE_NIGHT_YES to UiText.Resource(UiR.string.settings_dark_theme_value_enabled),
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM to UiText.Resource(UiR.string.settings_dark_theme_value_follow_system),
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY to UiText.Resource(UiR.string.settings_dark_theme_value_battery_saver),
                AppCompatDelegate.MODE_NIGHT_NO to UiText.Resource(UiR.string.settings_dark_theme_value_disabled)
            ).toMap()

            val appearanceDarkTheme = SettingsItem.ListItem.build(
                key = SettingsKeys.KEY_APPEARANCE_DARK_THEME,
                title = UiText.Resource(UiR.string.settings_dark_theme),
                values = darkThemeValues.keys.toList(),
                valueTitles = darkThemeValues.values.toList(),
                defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
            ) {
                summaryProvider = SettingsItem.SummaryProvider { item ->
                    val darkThemeValue = darkThemeValues[item.value ?: 0]

                    UiText.ResourceParams(
                        value = UiR.string.settings_dark_theme_current_value,
                        args = listOf(
                            darkThemeValue
                                ?: UiText.Resource(UiR.string.settings_dark_theme_current_value_unknown)
                        )
                    )
                }
            }
            val appearanceUseAmoledDarkTheme = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
                defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME,
                title = UiText.Resource(UiR.string.settings_amoled_dark_theme),
                summary = UiText.Resource(UiR.string.settings_amoled_dark_theme_description)
            )
            val appearanceUseDynamicColors = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_USE_DYNAMIC_COLORS,
                title = UiText.Resource(UiR.string.settings_dynamic_colors),
                isVisible = isSdkAtLeast(Build.VERSION_CODES.S),
                summary = UiText.Resource(UiR.string.settings_dynamic_colors_description),
                defaultValue = SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
            )

            val appearanceLanguage = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_APPEARANCE_LANGUAGE,
                title = UiText.Resource(UiR.string.settings_application_language),
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
                defaultValue = SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT
            ).apply {
                summaryProvider = SettingsItem.SummaryProvider { settingsItem ->
                    UiText.ResourceParams(
                        UiR.string.pref_message_fast_text_summary,
                        listOf(settingsItem.value?.ifEmpty { null })
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
            val debugUseBlur = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_APPEARANCE_BLUR,
                defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_APPEARANCE_BLUR,
                title = UiText.Simple("[WIP] Use blur"),
                summary = UiText.Simple("Adds blur wherever possible\nOn android 11 and older will have transparency instead of blurring"),
            )
            val debugShowEmojiButton = SettingsItem.Switch.build(
                key = SettingsKeys.KEY_SHOW_EMOJI_BUTTON,
                title = UiText.Simple("Show emoji button"),
                summary = UiText.Simple("Show emoji button in chat panel"),
                defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
            )

            val debugHideDebugList = SettingsItem.TitleSummary.build(
                key = SettingsKeys.KEY_DEBUG_HIDE_DEBUG_LIST,
                title = UiText.Simple("Hide debug list")
            )

            val accountList = listOf(
                accountTitle,
                accountLogOut
            )
            val generalList = listOf(
                generalTitle,
                generalUseContactNames
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
            val debugList = mutableListOf<SettingsItem<*>>()
            listOf(
                debugTitle,
                debugPerformCrash,
                debugShowCrashAlert,
                debugShowExactTimeOnTimeStamp,
                debugUseBlur,
                debugShowEmojiButton
            ).forEach(debugList::add)

            debugList += debugHideDebugList

            val settingsList = mutableListOf<SettingsItem<*>>()
            listOf(
                accountList,
                generalList,
                appearanceList,
                featuresList,
                visibilityList,
                debugList,
            ).forEach(settingsList::addAll)

            if (!isDebugSettingsShown()) {
                settingsList.removeAll(debugList)
            }

            screenState.setValue { old -> old.copy(settings = settingsList) }
        }
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
