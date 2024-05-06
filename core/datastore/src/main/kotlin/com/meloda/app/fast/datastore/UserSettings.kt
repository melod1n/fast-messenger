package com.meloda.app.fast.datastore

import android.content.Context
import com.meloda.app.fast.common.extensions.preferences
import com.meloda.app.fast.datastore.model.ThemeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface UserSettings {
    val multiline: StateFlow<Boolean>

    val theme: StateFlow<ThemeConfig>

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

class UserSettingsImpl(
    context: Context
) : UserSettings {
    override val multiline = MutableStateFlow(
        context.preferences.getBoolean(
            SettingsKeys.KEY_APPEARANCE_MULTILINE,
            SettingsKeys.DEFAULT_VALUE_MULTILINE
        )
    )

    override val theme = MutableStateFlow(ThemeConfig.EMPTY)

    override val language = MutableStateFlow(
        context.preferences.getString(
            SettingsKeys.KEY_APPEARANCE_LANGUAGE,
            SettingsKeys.DEFAULT_VALUE_APPEARANCE_LANGUAGE
        ).orEmpty()
    )

    override val languageChangedFromApp = MutableStateFlow(false)

    override val longPollBackground = MutableStateFlow(
        context.preferences.getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )
    )
    override val online = MutableStateFlow(
        context.preferences.getBoolean(
            SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
            SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
        )
    )

    // TODO: 05/05/2024, Danil Nikolaev: get default value
    override val debugSettingsEnabled = MutableStateFlow(false)

    // TODO: 05/05/2024, Danil Nikolaev: get default value
    override fun updateUsingDarkTheme() {
        useDarkThemeChanged(false)
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
