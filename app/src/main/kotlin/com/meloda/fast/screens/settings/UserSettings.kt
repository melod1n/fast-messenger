package com.meloda.fast.screens.settings

import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isDebugSettingsShown
import com.meloda.fast.ext.isUsingAmoledBackground
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.isUsingDynamicColors
import com.meloda.fast.screens.settings.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface UserSettings {
    val multiline: StateFlow<Boolean>

    val theme: AppTheme
    val themeFlow: StateFlow<AppTheme>

    val language: StateFlow<String>
    val languageChangedFromApp: StateFlow<Boolean>

    val longPollBackground: StateFlow<Boolean>
    val online: StateFlow<Boolean>

    val debugSettingsEnabled: StateFlow<Boolean>

    fun useDarkThemeChanged(use: Boolean)

    fun useAmoledThemeChanged(use: Boolean)

    fun useDynamicColorsChanged(use: Boolean)

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

    override val theme: AppTheme by AppThemePreferenceDelegate()
    override val themeFlow = MutableStateFlow(AppTheme.EMPTY)

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

    override fun useDarkThemeChanged(use: Boolean) {
        themeFlow.value = themeFlow.value.copy(
            usingDarkStyle = use
        )
    }

    override fun useAmoledThemeChanged(use: Boolean) {
        themeFlow.value = themeFlow.value.copy(
            usingAmoledBackground = use
        )
    }

    override fun useDynamicColorsChanged(use: Boolean) {
        themeFlow.value = themeFlow.value.copy(
            usingDynamicColors = use
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

    inner class AppThemePreferenceDelegate : ReadWriteProperty<Any?, AppTheme> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): AppTheme {
            return AppTheme(
                usingDarkStyle = isUsingDarkTheme(),
                usingDynamicColors = isUsingDynamicColors(),
                usingAmoledBackground = isUsingAmoledBackground()
            )
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: AppTheme) {
            themeFlow.value = value
        }
    }
}
