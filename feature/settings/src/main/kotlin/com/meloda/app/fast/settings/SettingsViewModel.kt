package com.meloda.app.fast.settings

import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.common.LongPollController
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.findWithIndex
import com.meloda.app.fast.common.extensions.isSdkAtLeast
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.model.DarkMode
import com.meloda.app.fast.common.model.LongPollState
import com.meloda.app.fast.common.model.UiText
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.datastore.AppSettings
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.model.database.AccountEntity
import com.meloda.app.fast.settings.model.SettingsItem
import com.meloda.app.fast.settings.model.SettingsScreenState
import com.meloda.app.fast.settings.model.SettingsShowOptions
import com.meloda.app.fast.settings.model.TextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.meloda.app.fast.ui.R as UiR

interface SettingsViewModel {

    val screenState: StateFlow<SettingsScreenState>
    val hapticType: StateFlow<HapticType?>

    fun onLogOutAlertDismissed()
    fun onLogOutAlertPositiveClick()

    fun onPerformCrashAlertDismissed()
    fun onPerformCrashPositiveButtonClicked()

    fun onSettingsItemClicked(key: String)
    fun onSettingsItemLongClicked(key: String)
    fun onSettingsItemChanged(key: String, newValue: Any?)

    fun onHapticPerformed()
}

class SettingsViewModelImpl(
    private val accountsRepository: AccountsRepository,
    private val userSettings: UserSettings,
    private val resources: Resources,
    private val longPollController: LongPollController
) : SettingsViewModel, ViewModel() {

    override val screenState = MutableStateFlow(SettingsScreenState.EMPTY)
    override val hapticType = MutableStateFlow<HapticType?>(null)

    private val settings = MutableStateFlow<List<SettingsItem<*>>>(emptyList())

    init {
        createSettings()
    }

    override fun onLogOutAlertDismissed() {
        emitShowOptions { old -> old.copy(showLogOut = false) }
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

    override fun onPerformCrashAlertDismissed() {
        emitShowOptions { old -> old.copy(showPerformCrash = false) }
    }

    override fun onPerformCrashPositiveButtonClicked() {
        throw Exception("Test exception")
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
                val showDebugCategory = AppSettings.Debug.showDebugCategory
                if (!showDebugCategory) return

                onSettingsItemChanged(key, false)

                createSettings()

                hapticType.update { HapticType.REJECT }
                screenState.setValue { old -> old.copy(showDebugOptions = false) }
            }
        }
    }

    override fun onSettingsItemLongClicked(key: String) {
        when (key) {
            SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS -> {
                if (AppSettings.Debug.showDebugCategory) return

                onSettingsItemChanged(key, true)

                createSettings()

                hapticType.update { HapticType.LONG_PRESS }
                screenState.setValue { old -> old.copy(showDebugOptions = true) }
            }
        }
    }

    override fun onSettingsItemChanged(key: String, newValue: Any?) {
        settings.value.findWithIndex { it.key == key }?.let { (index, item) ->
            item.updateValue(newValue)
            item.updateText()

            screenState.setValue { old ->
                old.copy(
                    settings = old.settings.toMutableList().apply {
                        this[index] = item.asPresentation(resources)
                    }
                )
            }
        }

        when (key) {
            SettingsKeys.KEY_USE_CONTACT_NAMES -> {
                val isUsing = newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
                userSettings.onUseContactNamesChanged(isUsing)
            }

            SettingsKeys.KEY_ENABLE_PULL_TO_REFRESH -> {
                val enable =
                    newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_ENABLE_PULL_TO_REFRESH
                userSettings.onEnablePullToRefreshChanged(enable)
            }


            SettingsKeys.KEY_APPEARANCE_MULTILINE -> {
                val isUsing = newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_MULTILINE
                userSettings.onEnableMultilineChanged(isUsing)
            }

            SettingsKeys.KEY_APPEARANCE_DARK_MODE -> {
                val newMode = newValue as? Int ?: SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_MODE
                AppCompatDelegate.setDefaultNightMode(newMode)
                userSettings.onDarkModeChanged(DarkMode.parse(newMode))
            }

            SettingsKeys.KEY_APPEARANCE_AMOLED_THEME -> {
                val isUsing =
                    newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME
                userSettings.onEnableAmoledDarkChanged(isUsing)
            }

            SettingsKeys.KEY_USE_DYNAMIC_COLORS -> {
                val isUsing = newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
                userSettings.onEnableDynamicColorsChanged(isUsing)
            }

            SettingsKeys.KEY_APPEARANCE_LANGUAGE -> {
                val newLanguage = newValue as? String ?: SettingsKeys.DEFAULT_APPEARANCE_LANGUAGE
                userSettings.onAppLanguageChanged(newLanguage)
            }


            SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT -> {
                val newText = newValue as? String ?: SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT
                userSettings.onFastTextChanged(newText)
            }


            SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS -> {
                val isUsing = newValue as? Boolean
                    ?: SettingsKeys.DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS
                userSettings.onSendOnlineStatusChanged(isUsing)
            }

            SettingsKeys.KEY_DEBUG_SHOW_CRASH_ALERT -> {
                val show = newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
                userSettings.onShowAlertAfterCrashChanged(show)
            }

            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND -> {
                val inBackground = newValue as? Boolean
                    ?: SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
                userSettings.onLongPollInBackgroundChanged(inBackground)

                longPollController.setStateToApply(
                    longPollController.stateToApply.value.let { state ->
                        if (state.isLaunched()) {
                            if (inBackground) LongPollState.Background
                            else LongPollState.InApp
                        } else state
                    }
                )
            }

            SettingsKeys.KEY_APPEARANCE_USE_BLUR -> {
                val isUsing =
                    newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_KEY_APPEARANCE_USE_BLUR
                userSettings.onUseBlurChanged(isUsing)
            }

            SettingsKeys.KEY_SHOW_EMOJI_BUTTON -> {
                val show = newValue as? Boolean ?: SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
                userSettings.onShowEmojiButtonChanged(show)
            }

            SettingsKeys.KEY_SHOW_DEBUG_CATEGORY -> {
                val show = newValue as? Boolean ?: false
                userSettings.onShowDebugCategoryChanged(show)
            }
        }
    }

    override fun onHapticPerformed() {
        hapticType.update { null }
    }

    private fun emitShowOptions(function: (SettingsShowOptions) -> SettingsShowOptions) {
        val newShowOptions = function.invoke(screenState.value.showOptions)
        screenState.setValue { old -> old.copy(showOptions = newShowOptions) }
    }

    private fun createSettings() {
        val accountVisible = UserConfig.isLoggedIn()
        val accountTitle = SettingsItem.Title(
            key = SettingsKeys.KEY_ACCOUNT,
            title = UiText.Resource(UiR.string.settings_account_title),
            isVisible = accountVisible
        )
        val accountLogOut = SettingsItem.TitleText(
            key = SettingsKeys.KEY_ACCOUNT_LOGOUT,
            title = UiText.Resource(UiR.string.settings_account_logout_title),
            text = UiText.Resource(UiR.string.settings_account_logout_summary),
            isVisible = accountVisible
        )

        val generalTitle = SettingsItem.Title(
            key = SettingsKeys.KEY_GENERAL,
            title = UiText.Resource(UiR.string.settings_general_title)
        )
        val generalUseContactNames = SettingsItem.Switch(
            key = SettingsKeys.KEY_USE_CONTACT_NAMES,
            title = UiText.Resource(UiR.string.settings_general_contact_names_title),
            text = UiText.Resource(UiR.string.settings_general_contact_names_summary),
            defaultValue = SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
        )
        val generalEnablePullToRefresh = SettingsItem.Switch(
            key = SettingsKeys.KEY_ENABLE_PULL_TO_REFRESH,
            defaultValue = SettingsKeys.DEFAULT_VALUE_ENABLE_PULL_TO_REFRESH,
            title = UiText.Resource(UiR.string.settings_general_enable_pull_to_refresh_title)
        )

        val appearanceTitle = SettingsItem.Title(
            key = SettingsKeys.KEY_APPEARANCE,
            title = UiText.Resource(UiR.string.settings_appearance_title)
        )
        val appearanceMultiline = SettingsItem.Switch(
            key = SettingsKeys.KEY_APPEARANCE_MULTILINE,
            defaultValue = SettingsKeys.DEFAULT_VALUE_MULTILINE,
            title = UiText.Resource(UiR.string.settings_appearance_multiline_title),
            text = UiText.Resource(UiR.string.settings_appearance_multiline_summary)
        )

        val darkThemeValues = listOf(
            DarkMode.ENABLED to UiText.Resource(UiR.string.settings_dark_theme_value_enabled),
            DarkMode.FOLLOW_SYSTEM to UiText.Resource(UiR.string.settings_dark_theme_value_follow_system),
            DarkMode.AUTO_BATTERY to UiText.Resource(UiR.string.settings_dark_theme_value_battery_saver),
            DarkMode.DISABLED to UiText.Resource(UiR.string.settings_dark_theme_value_disabled)
        ).toMap()

        val appearanceDarkTheme = SettingsItem.ListItem(
            key = SettingsKeys.KEY_APPEARANCE_DARK_MODE,
            title = UiText.Resource(UiR.string.settings_dark_theme),
            valueClass = Int::class,
            defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_MODE,
            titles = darkThemeValues.values.toList(),
            values = darkThemeValues.keys.toList().map(DarkMode::value)
        ).apply {
            textProvider = TextProvider { item ->
                val darkThemeValue = darkThemeValues[DarkMode.parse(item.value)]

                UiText.ResourceParams(
                    value = UiR.string.settings_dark_theme_current_value,
                    args = listOf(
                        darkThemeValue
                            ?: UiText.Resource(UiR.string.settings_dark_theme_current_value_unknown)
                    )
                )
            }
        }
        val appearanceUseAmoledDarkTheme = SettingsItem.Switch(
            key = SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
            defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME,
            title = UiText.Resource(UiR.string.settings_amoled_dark_theme),
            text = UiText.Resource(UiR.string.settings_amoled_dark_theme_description)
        )
        val appearanceUseDynamicColors = SettingsItem.Switch(
            key = SettingsKeys.KEY_USE_DYNAMIC_COLORS,
            title = UiText.Resource(UiR.string.settings_dynamic_colors),
            isVisible = isSdkAtLeast(Build.VERSION_CODES.S),
            text = UiText.Resource(UiR.string.settings_dynamic_colors_description),
            defaultValue = SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )

        val appearanceLanguage = SettingsItem.TitleText(
            key = SettingsKeys.KEY_APPEARANCE_LANGUAGE,
            title = UiText.Resource(UiR.string.settings_application_language),
        )

        val featuresTitle = SettingsItem.Title(
            key = "features",
            title = UiText.Resource(UiR.string.settings_features_title)
        )
        val featuresFastText = SettingsItem.TextField(
            key = SettingsKeys.KEY_FEATURES_FAST_TEXT,
            title = UiText.Resource(UiR.string.settings_features_fast_text_title),
            defaultValue = SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT
        ).apply {
            textProvider = TextProvider { settingsItem ->
                UiText.ResourceParams(
                    UiR.string.pref_message_fast_text_summary,
                    listOf(settingsItem.value)
                )
            }
        }
        val debugLongPollBackground = SettingsItem.Switch(
            key = SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            defaultValue = SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND,
            title = UiText.Resource(UiR.string.settings_features_long_poll_in_background_title),
            text = UiText.Resource(UiR.string.settings_features_long_poll_in_background_summary)
        )

        val activityTitle = SettingsItem.Title(
            key = "activity",
            title = UiText.Resource(UiR.string.settings_activity_title)
        )
        val visibilitySendOnlineStatus = SettingsItem.Switch(
            key = SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS,
            defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS,
            title = UiText.Resource(UiR.string.settings_activity_send_online_title),
            text = UiText.Resource(UiR.string.settings_activity_send_online_summary)
        )

        val debugTitle = SettingsItem.Title(
            key = "debug",
            title = UiText.Resource(UiR.string.settings_debug_title)
        )
        val debugPerformCrash = SettingsItem.TitleText(
            key = SettingsKeys.KEY_DEBUG_PERFORM_CRASH,
            title = UiText.Simple("Perform crash"),
            text = UiText.Simple("App will be crashed. Obviously")
        )
        val debugShowCrashAlert = SettingsItem.Switch(
            key = SettingsKeys.KEY_DEBUG_SHOW_CRASH_ALERT,
            defaultValue = true,
            title = UiText.Simple("Show alert after crash"),
            text = UiText.Simple("Shows alert dialog with stacktrace after app crashed\n(it will be not shown if you perform crash manually)")
        )
        val debugUseBlur = SettingsItem.Switch(
            key = SettingsKeys.KEY_APPEARANCE_USE_BLUR,
            defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_APPEARANCE_USE_BLUR,
            title = UiText.Simple("[WIP] Use blur"),
            text = UiText.Simple("Adds blur wherever possible\nOn android 11 and older will have transparency instead of blurring"),
        )
        val debugShowEmojiButton = SettingsItem.Switch(
            key = SettingsKeys.KEY_SHOW_EMOJI_BUTTON,
            title = UiText.Simple("Show emoji button"),
            text = UiText.Simple("Show emoji button in chat panel"),
            defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
        )

        val debugHideDebugList = SettingsItem.TitleText(
            key = SettingsKeys.KEY_DEBUG_HIDE_DEBUG_LIST,
            title = UiText.Simple("Hide debug list")
        )

        val accountList = listOf(
            accountTitle,
            accountLogOut
        )
        val generalList = listOf(
            generalTitle,
            generalUseContactNames,
            generalEnablePullToRefresh
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
            featuresFastText
        )
        val visibilityList = listOf(
            activityTitle,
            visibilitySendOnlineStatus,
        )
        val debugList = mutableListOf<SettingsItem<*>>()
        listOf(
            debugTitle,
            debugPerformCrash,
            debugShowCrashAlert,
            debugLongPollBackground,
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

        if (!AppSettings.Debug.showDebugCategory) {
            settingsList.removeAll(debugList)
        }

        emitSettings(settingsList)
    }

    private fun emitSettings(newSettings: List<SettingsItem<*>>) {
        settings.update { newSettings }

        val uiSettings = newSettings.map { item ->
            item.asPresentation(resources)
        }

        screenState.setValue { old -> old.copy(settings = uiSettings) }
    }
}

enum class HapticType {
    LONG_PRESS, REJECT;

    fun getHaptic(): Int = when (this) {
        LONG_PRESS -> HapticFeedbackConstantsCompat.LONG_PRESS
        REJECT -> HapticFeedbackConstantsCompat.REJECT
    }
}
