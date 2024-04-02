package com.meloda.fast.screens.settings

import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isDebugSettingsShown
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.screens.settings.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface UserSettings {
    val multiline: StateFlow<Boolean>

    val theme: StateFlow<AppTheme>

    val language: StateFlow<String>
    val languageChangedFromApp: StateFlow<Boolean>

    val longPollBackground: StateFlow<Boolean>
    val online: StateFlow<Boolean>

    val debugSettingsEnabled: StateFlow<Boolean>

    fun updateUsingDarkTheme()

    fun useDarkThemeChanged(use: Boolean)

    fun useAmoledThemeChanged(use: Boolean)

    fun useDynamicColorsChanged(use: Boolean)

    fun useBlurChanged(use: Boolean)

    fun useMultiline(use: Boolean)

    fun setLongPollBackground(background: Boolean)

    fun setOnline(use: Boolean)

    fun setLanguage(newLanguage: String, withUpdate: Boolean = true)

    fun onLanguageChanged()

    fun enableDebugSettings(enable: Boolean)
}

class UserSettingsImpl : UserSettings {
    override val multiline = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_APPEARANCE_MULTILINE,
            SettingsKeys.DEFAULT_VALUE_MULTILINE
        )
    )

    override val theme = MutableStateFlow(AppTheme.EMPTY)

    override val language = MutableStateFlow(
        AppGlobal.preferences.getString(
            SettingsKeys.KEY_APPEARANCE_LANGUAGE,
            SettingsKeys.DEFAULT_VALUE_APPEARANCE_LANGUAGE
        ).orEmpty()
    )

    override val languageChangedFromApp = MutableStateFlow(false)

    override val longPollBackground = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )
    )
    override val online = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
            SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
        )
    )

    override val debugSettingsEnabled = MutableStateFlow(isDebugSettingsShown())

    override fun updateUsingDarkTheme() {
        useDarkThemeChanged(isUsingDarkTheme())
    }

    override fun useDarkThemeChanged(use: Boolean) {
        theme.value = theme.value.copy(
            usingDarkStyle = use
        )
    }

    override fun useAmoledThemeChanged(use: Boolean) {
        theme.value = theme.value.copy(
            usingAmoledBackground = use
        )
    }

    override fun useDynamicColorsChanged(use: Boolean) {
        theme.value = theme.value.copy(
            usingDynamicColors = use
        )
    }

    override fun useBlurChanged(use: Boolean) {
        theme.value = theme.value.copy(
            usingBlur = use
        )
    }

    override fun useMultiline(use: Boolean) {
        multiline.value = use
    }

    override fun setLongPollBackground(background: Boolean) {
        longPollBackground.value = background
    }

    override fun setOnline(use: Boolean) {
        online.value = use
    }

    override fun setLanguage(newLanguage: String, withUpdate: Boolean) {
        if (withUpdate) {
            languageChangedFromApp.update { true }
        }
        language.update { newLanguage }
    }

    override fun onLanguageChanged() {
        languageChangedFromApp.update { false }
    }

    override fun enableDebugSettings(enable: Boolean) {
        debugSettingsEnabled.update { enable }
    }
}
