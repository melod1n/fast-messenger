package com.meloda.fast.screens.settings

import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isUsingDarkTheme
import com.meloda.fast.ext.isUsingDynamicColors
import com.meloda.fast.screens.settings.model.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface UserSettings {
    val theme: AppTheme
    val themeFlow: StateFlow<AppTheme>
    val multiline: StateFlow<Boolean>

    fun useDarkThemeChanged(use: Boolean)

    fun useDynamicColorsChanged(use: Boolean)

    fun useMultiline(use: Boolean)
}

class UserSettingsImpl : UserSettings {
    override val theme: AppTheme by AppThemePreferenceDelegate()
    override val themeFlow = MutableStateFlow(AppTheme.EMPTY)
    override val multiline = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_APPEARANCE_MULTILINE,
            SettingsKeys.DEFAULT_VALUE_MULTILINE
        )
    )

    override fun useDarkThemeChanged(use: Boolean) {
        themeFlow.value = themeFlow.value.copy(
            usingDarkStyle = use
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

    inner class AppThemePreferenceDelegate : ReadWriteProperty<Any?, AppTheme> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): AppTheme {
            return AppTheme(
                usingDarkStyle = isUsingDarkTheme(),
                usingDynamicColors = isUsingDynamicColors()
            )
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: AppTheme) {
            themeFlow.value = value
        }
    }
}
