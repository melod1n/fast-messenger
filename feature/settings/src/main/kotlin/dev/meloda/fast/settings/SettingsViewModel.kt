package dev.meloda.fast.settings

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.findWithIndex
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.model.DarkMode
import dev.meloda.fast.common.model.LogLevel
import dev.meloda.fast.common.model.LongPollState
import dev.meloda.fast.common.model.UiText
import dev.meloda.fast.common.model.parseString
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.db.AccountsRepository
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.SettingsKeys
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.GetCurrentAccountUseCase
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.model.database.AccountEntity
import dev.meloda.fast.settings.model.HapticType
import dev.meloda.fast.settings.model.SettingsDialog
import dev.meloda.fast.settings.model.SettingsItem
import dev.meloda.fast.settings.model.SettingsScreenState
import dev.meloda.fast.settings.model.TextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.meloda.fast.ui.R

class SettingsViewModel(
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val accountsRepository: AccountsRepository,
    private val getCurrentAccountUseCase: GetCurrentAccountUseCase,
    private val userSettings: UserSettings,
    private val resources: Resources,
    private val longPollController: LongPollController
) : ViewModel() {

    private val _screenState = MutableStateFlow(SettingsScreenState.EMPTY)
    val screenState = _screenState.asStateFlow()

    private val _hapticType = MutableStateFlow<HapticType?>(null)
    val hapticType = _hapticType.asStateFlow()

    private val _dialog = MutableStateFlow<SettingsDialog?>(null)
    val dialog = _dialog.asStateFlow()

    private val _isNeedToRestart = MutableStateFlow(false)
    val isNeedToRestart = _isNeedToRestart.asStateFlow()

    private val settings = MutableStateFlow<List<SettingsItem<*>>>(emptyList())

    init {
        createSettings()
    }

    fun onDialogConfirmed(dialog: SettingsDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is SettingsDialog.LogOut -> onLogOutAlertPositiveClick()
            is SettingsDialog.PerformCrash -> onPerformCrashPositiveButtonClicked()

            is SettingsDialog.ImportAuthData -> {
                val accessToken = bundle.getString("ACCESS_TOKEN") ?: return
                val exchangeToken = bundle.getString("EXCHANGE_TOKEN")
                val trustedHash = bundle.getString("TRUSTED_HASH")

                viewModelScope.launch(Dispatchers.IO) {
                    val oldToken = UserConfig.accessToken

                    UserConfig.accessToken = accessToken

                    loadUserByIdUseCase(
                        userId = null,
                        fields = VkConstants.USER_FIELDS
                    ).listenValue(viewModelScope) { state ->
                        state.processState(
                            error = { error ->
                                UserConfig.accessToken = oldToken
                            },
                            success = { user ->
                                if (user == null) return@listenValue

                                UserConfig.currentUserId = user.id

                                val account = getCurrentAccountUseCase()
                                    ?.copy(
                                        userId = user.id,
                                        accessToken = accessToken,
                                        fastToken = null,
                                        exchangeToken = exchangeToken,
                                        trustedHash = trustedHash
                                    ) ?: AccountEntity(
                                    userId = user.id,
                                    accessToken = accessToken,
                                    fastToken = null,
                                    trustedHash = trustedHash,
                                    exchangeToken = exchangeToken
                                )

                                accountsRepository.storeAccounts(listOf(account))

                                _isNeedToRestart.setValue { true }
                            }
                        )
                    }
                }
            }

            is SettingsDialog.ExportAuthData -> Unit
        }
    }

    fun onDialogDismissed(dialog: SettingsDialog) {
        when (dialog) {
            is SettingsDialog.LogOut -> Unit
            is SettingsDialog.PerformCrash -> Unit
            is SettingsDialog.ImportAuthData -> Unit
            is SettingsDialog.ExportAuthData -> Unit
        }

        _dialog.setValue { null }
    }

    fun onDialogItemPicked(dialog: SettingsDialog, bundle: Bundle) {
        when (dialog) {
            is SettingsDialog.LogOut -> Unit
            is SettingsDialog.PerformCrash -> Unit
            is SettingsDialog.ImportAuthData -> Unit
            is SettingsDialog.ExportAuthData -> Unit
        }
    }

    fun onLogOutAlertPositiveClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val tasks = listOf(
                async {
                    accountsRepository.storeAccounts(
                        listOf(
                            AccountEntity(
                                userId = UserConfig.userId,
                                accessToken = "",
                                fastToken = UserConfig.fastToken,
                                trustedHash = UserConfig.trustedHash,
                                exchangeToken = null
                            )
                        )
                    )
                },
                async { UserConfig.clear() }
            )

            tasks.awaitAll()
        }
    }

    fun onPerformCrashPositiveButtonClicked() {
        throw Exception("Test exception")
    }

    fun onSettingsItemClicked(key: String) {
        when (key) {
            SettingsKeys.KEY_ACCOUNT_LOGOUT -> {
                _dialog.setValue { SettingsDialog.LogOut }
            }

            SettingsKeys.KEY_DEBUG_PERFORM_CRASH -> {
                _dialog.setValue { SettingsDialog.PerformCrash }
            }

            SettingsKeys.KEY_DEBUG_IMPORT_AUTH_DATA -> {
                _dialog.setValue { SettingsDialog.ImportAuthData }
            }

            SettingsKeys.KEY_DEBUG_EXPORT_AUTH_DATA -> {
                _dialog.setValue {
                    SettingsDialog.ExportAuthData(
                        accessToken = UserConfig.accessToken,
                        exchangeToken = UserConfig.exchangeToken,
                        trustedHash = UserConfig.trustedHash
                    )
                }
            }

            SettingsKeys.KEY_DEBUG_HIDE_DEBUG_LIST -> {
                if (!AppSettings.Debug.showDebugCategory) return

                AppSettings.Debug.showDebugCategory = false
                userSettings.onShowDebugCategoryChanged(false)

                createSettings()

                _hapticType.update { HapticType.REJECT }
                _screenState.setValue { old -> old.copy(showDebugOptions = false) }
            }
        }
    }

    fun onSettingsItemLongClicked(key: String) {
        when (key) {
            SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS -> {
                if (AppSettings.Debug.showDebugCategory) return

                AppSettings.Debug.showDebugCategory = true
                userSettings.onShowDebugCategoryChanged(true)

                createSettings()

                _hapticType.update { HapticType.LONG_PRESS }
                _screenState.setValue { old -> old.copy(showDebugOptions = true) }
            }
        }
    }

    fun onSettingsItemChanged(key: String, newValue: Any?) {
        settings.value.findWithIndex { it.key == key }?.let { (index, item) ->
            item.updateValue(newValue)
            item.updateText()

            _screenState.setValue { old ->
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

            SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS -> {
                val isUsing = newValue as? Boolean
                    ?: SettingsKeys.DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS
                userSettings.onSendOnlineStatusChanged(isUsing)
            }

            SettingsKeys.KEY_LONG_POLL_IN_BACKGROUND -> {
                val inBackground = newValue as? Boolean
                    ?: SettingsKeys.DEFAULT_LONG_POLL_IN_BACKGROUND
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

            SettingsKeys.KEY_USE_BLUR -> {
                val isUsing =
                    newValue as? Boolean ?: SettingsKeys.DEFAULT_USE_BLUR
                userSettings.onUseBlurChanged(isUsing)
            }

            SettingsKeys.KEY_USE_SYSTEM_FONT -> {
                val use = newValue as? Boolean ?: SettingsKeys.DEFAULT_USE_SYSTEM_FONT
                userSettings.onUseSystemFontChanged(use)
            }

            SettingsKeys.KEY_SHOW_DEBUG_CATEGORY -> {
                val show = newValue as? Boolean ?: false
                userSettings.onShowDebugCategoryChanged(show)
            }
        }
    }

    fun onHapticPerformed() {
        _hapticType.update { null }
    }

    private fun createSettings() {
        val accountVisible = UserConfig.isLoggedIn()
        val accountTitle = SettingsItem.Title(
            key = SettingsKeys.KEY_ACCOUNT,
            title = UiText.Resource(R.string.settings_account_title),
            isVisible = accountVisible
        )
        val accountLogOut = SettingsItem.TitleText(
            key = SettingsKeys.KEY_ACCOUNT_LOGOUT,
            title = UiText.Resource(R.string.settings_account_logout_title),
            text = UiText.Resource(R.string.settings_account_logout_summary),
            isVisible = accountVisible
        )

        val generalTitle = SettingsItem.Title(
            key = SettingsKeys.KEY_GENERAL,
            title = UiText.Resource(R.string.settings_general_title)
        )
        val generalUseContactNames = SettingsItem.Switch(
            key = SettingsKeys.KEY_USE_CONTACT_NAMES,
            title = UiText.Resource(R.string.settings_general_contact_names_title),
            text = UiText.Resource(R.string.settings_general_contact_names_summary),
            defaultValue = SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
        )
        val generalShowEmojiButton = SettingsItem.Switch(
            key = SettingsKeys.KEY_SHOW_EMOJI_BUTTON,
            title = UiText.Resource(R.string.settings_general_show_emoji_button_title),
            text = UiText.Resource(R.string.settings_general_show_emoji_button_summary),
            defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
        )
        val generalShowAttachmentButton = SettingsItem.Switch(
            key = SettingsKeys.KEY_SHOW_ATTACHMENT_BUTTON,
            title = UiText.Resource(R.string.settings_general_show_attachment_button_title),
            text = UiText.Resource(R.string.settings_general_show_attachment_button_summary),
            defaultValue = SettingsKeys.DEFAULT_VALUE_SHOW_ATTACHMENT_BUTTON
        )
        val generalEnableHaptic = SettingsItem.Switch(
            key = SettingsKeys.KEY_ENABLE_HAPTIC,
            defaultValue = SettingsKeys.DEFAULT_ENABLE_HAPTIC,
            title = UiText.Resource(R.string.settings_general_enable_haptic_title)
        )

        val appearanceTitle = SettingsItem.Title(
            key = SettingsKeys.KEY_APPEARANCE,
            title = UiText.Resource(R.string.settings_appearance_title)
        )
        val appearanceMultiline = SettingsItem.Switch(
            key = SettingsKeys.KEY_APPEARANCE_MULTILINE,
            defaultValue = SettingsKeys.DEFAULT_VALUE_MULTILINE,
            title = UiText.Resource(R.string.settings_appearance_multiline_title),
            text = UiText.Resource(R.string.settings_appearance_multiline_summary)
        )

        val darkThemeValues = listOf(
            DarkMode.ENABLED to UiText.Resource(R.string.settings_dark_theme_value_enabled),
            DarkMode.FOLLOW_SYSTEM to UiText.Resource(R.string.settings_dark_theme_value_follow_system),
            DarkMode.AUTO_BATTERY to UiText.Resource(R.string.settings_dark_theme_value_battery_saver),
            DarkMode.DISABLED to UiText.Resource(R.string.settings_dark_theme_value_disabled)
        ).toMap()

        val appearanceDarkTheme = SettingsItem.ListItem(
            key = SettingsKeys.KEY_APPEARANCE_DARK_MODE,
            title = UiText.Resource(R.string.settings_dark_theme),
            valueClass = Int::class,
            defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_MODE,
            titles = darkThemeValues.values.toList(),
            values = darkThemeValues.keys.toList().map(DarkMode::value)
        ).apply {
            textProvider = TextProvider { item ->
                val darkThemeValue = darkThemeValues[DarkMode.parse(item.value)]

                UiText.ResourceParams(
                    value = R.string.settings_dark_theme_current_value,
                    args = listOf(
                        darkThemeValue
                            ?: UiText.Resource(R.string.settings_dark_theme_current_value_unknown)
                    )
                )
            }
        }
        val appearanceUseAmoledDarkTheme = SettingsItem.Switch(
            key = SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
            defaultValue = SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME,
            title = UiText.Resource(R.string.settings_amoled_dark_theme),
            text = UiText.Resource(R.string.settings_amoled_dark_theme_description)
        )
        val appearanceUseDynamicColors = SettingsItem.Switch(
            key = SettingsKeys.KEY_USE_DYNAMIC_COLORS,
            title = UiText.Resource(R.string.settings_dynamic_colors),
            isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
            text = UiText.Resource(R.string.settings_dynamic_colors_description),
            defaultValue = SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
        )
        val appearanceUseSystemFont = SettingsItem.Switch(
            key = SettingsKeys.KEY_USE_SYSTEM_FONT,
            defaultValue = SettingsKeys.DEFAULT_USE_SYSTEM_FONT,
            title = UiText.Resource(R.string.settings_appearance_use_system_font_title)
        )
        val appearanceLanguage = SettingsItem.TitleText(
            key = SettingsKeys.KEY_APPEARANCE_LANGUAGE,
            title = UiText.Resource(R.string.settings_application_language),
        )

        val featuresTitle = SettingsItem.Title(
            key = "features",
            title = UiText.Resource(R.string.settings_features_title)
        )
        val featuresFastText = SettingsItem.TextField(
            key = SettingsKeys.KEY_FEATURES_FAST_TEXT,
            title = UiText.Resource(R.string.settings_features_fast_text_title),
            defaultValue = SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT
        ).apply {
            textProvider = TextProvider { settingsItem ->
                UiText.ResourceParams(
                    R.string.pref_message_fast_text_summary,
                    listOf(settingsItem.value)
                )
            }
        }

        val activityTitle = SettingsItem.Title(
            key = "activity",
            title = UiText.Resource(R.string.settings_activity_title)
        )
        val visibilitySendOnlineStatus = SettingsItem.Switch(
            key = SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS,
            defaultValue = SettingsKeys.DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS,
            title = UiText.Resource(R.string.settings_activity_send_online_title),
            text = UiText.Resource(R.string.settings_activity_send_online_summary)
        )

        val experimentalTitle = SettingsItem.Title(
            key = "experimental",
            title = UiText.Resource(R.string.settings_experimental_title)
        )
        val experimentalLongPollBackground = SettingsItem.Switch(
            key = SettingsKeys.KEY_LONG_POLL_IN_BACKGROUND,
            defaultValue = SettingsKeys.DEFAULT_LONG_POLL_IN_BACKGROUND,
            title = UiText.Resource(R.string.settings_features_long_poll_in_background_title),
            text = UiText.Resource(R.string.settings_features_long_poll_in_background_summary)
        )
        val experimentalShowTimeInActionMessages = SettingsItem.Switch(
            key = SettingsKeys.KEY_SHOW_TIME_IN_ACTION_MESSAGES,
            defaultValue = SettingsKeys.DEFAULT_SHOW_TIME_IN_ACTION_MESSAGES,
            title = UiText.Resource(R.string.settings_features_show_time_in_action_messages_title)
        )
        val experimentalUseBlur = SettingsItem.Switch(
            key = SettingsKeys.KEY_USE_BLUR,
            defaultValue = SettingsKeys.DEFAULT_USE_BLUR,
            title = UiText.Resource(R.string.settings_experimental_use_blur_title),
            text = UiText.Resource(R.string.settings_experimental_use_blur_summary)
        )
        val enableAnimations = SettingsItem.Switch(
            key = SettingsKeys.KEY_MORE_ANIMATIONS,
            defaultValue = SettingsKeys.DEFAULT_MORE_ANIMATIONS,
            title = UiText.Resource(R.string.settings_experimental_more_animations_title),
            text = UiText.Resource(R.string.settings_experimental_more_animations_summary)
        )

        val debugTitle = SettingsItem.Title(
            key = "debug",
            title = UiText.Resource(R.string.settings_debug_title)
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

        val logLevelValues = listOf(
            LogLevel.NONE to UiText.Simple("None"),
            LogLevel.BASIC to UiText.Simple("Basic"),
            LogLevel.HEADERS to UiText.Simple("Headers"),
            LogLevel.BODY to UiText.Simple("Body")
        ).toMap()

        val debugNetworkLogLevel = SettingsItem.ListItem(
            key = SettingsKeys.KEY_DEBUG_NETWORK_LOG_LEVEL,
            title = UiText.Simple("Network log level"),
            valueClass = Int::class,
            defaultValue = SettingsKeys.DEFAULT_NETWORK_LOG_LEVEL,
            titles = logLevelValues.values.toList(),
            values = logLevelValues.keys.toList().map(LogLevel::value)
        ).apply {
            textProvider = TextProvider { item ->
                val textValue = logLevelValues[LogLevel.parse(item.value)].parseString(resources)

                UiText.Simple("Current value: $textValue")
            }
        }

        val debugImportAuthData = SettingsItem.TitleText(
            key = SettingsKeys.KEY_DEBUG_IMPORT_AUTH_DATA,
            title = UiText.Simple("Import auth data"),
            text = UiText.Simple("App will be restarted")
        )
        val debugExportAuthData = SettingsItem.TitleText(
            key = SettingsKeys.KEY_DEBUG_EXPORT_AUTH_DATA,
            title = UiText.Simple("Export auth data"),
            text = UiText.Simple("Be careful with this data. If another person gets it, your account will be at risk"),
            isVisible = UserConfig.isLoggedIn()
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
            generalShowEmojiButton,
            generalShowAttachmentButton,
            generalEnableHaptic
        )
        val appearanceList = listOf(
            appearanceTitle,
            appearanceMultiline,
            appearanceDarkTheme,
            appearanceUseAmoledDarkTheme,
            appearanceUseDynamicColors,
            appearanceUseSystemFont,
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
        val experimentalList = listOf(
            experimentalTitle,
            experimentalLongPollBackground,
            experimentalShowTimeInActionMessages,
            experimentalUseBlur,
            enableAnimations
        )
        val debugList = mutableListOf<SettingsItem<*>>()
        listOf(
            debugTitle,
            debugPerformCrash,
            debugShowCrashAlert,
            debugNetworkLogLevel,
            debugImportAuthData,
            debugExportAuthData
        ).forEach(debugList::add)

        debugList += debugHideDebugList

        val settingsList = mutableListOf<SettingsItem<*>>()
        listOf(
            accountList,
            generalList,
            appearanceList,
            featuresList,
            visibilityList,
            experimentalList,
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

        _screenState.setValue { old -> old.copy(settings = uiSettings) }
    }
}
