package com.meloda.app.fast.datastore

import android.content.res.Resources
import android.os.PowerManager
import android.util.Log
import com.meloda.app.fast.datastore.model.LongPollState
import com.meloda.app.fast.datastore.model.ThemeConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface UserSettings {
    val theme: StateFlow<ThemeConfig>
    val longPollStateToApply: StateFlow<LongPollState>
    val longPollCurrentState: StateFlow<LongPollState>
    val online: StateFlow<Boolean>
    val debugSettingsEnabled: StateFlow<Boolean>
    val useContactNames: StateFlow<Boolean>
    val language: StateFlow<String>

    fun updateUsingDarkTheme()
    fun useDarkThemeChanged(use: Boolean)
    fun useAmoledThemeChanged(use: Boolean)
    fun useDynamicColorsChanged(use: Boolean)
    fun useBlurChanged(use: Boolean)
    fun useMultiline(use: Boolean)
    fun setLongPollStateToApply(newState: LongPollState)
    fun updateLongPollCurrentState(currentState: LongPollState)
    fun setOnline(use: Boolean)
    fun enableDebugSettings(enable: Boolean)
    fun onUseContactNamesChanged(use: Boolean)
    fun onLanguageChanged(newLanguage: String)
}

class UserSettingsImpl(
    private val resources: Resources,
    private val powerManager: PowerManager
) : UserSettings {

    override val theme = MutableStateFlow(
        ThemeConfig(
            usingDarkStyle = isUsingDarkMode(resources, powerManager),
            usingDynamicColors = isUsingDynamicColors(),
            selectedColorScheme = selectedColorScheme(),
            usingAmoledBackground = isUsingAmoledBackground(),
            usingBlur = isUsingBlur(),
            multiline = isMultiline(),
            isDeviceCompact = false
        )
    )

    override val longPollStateToApply = MutableStateFlow<LongPollState>(LongPollState.Stopped)
    override val longPollCurrentState = MutableStateFlow<LongPollState>(LongPollState.Stopped)

    override val online = MutableStateFlow(
        SettingsController.getBoolean(
            SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS,
            SettingsKeys.DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS
        )
    )

    override val debugSettingsEnabled = MutableStateFlow(
        SettingsController.getBoolean(
            SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
            false
        )
    )

    override val useContactNames = MutableStateFlow(
        SettingsController.getBoolean(
            SettingsKeys.KEY_USE_CONTACT_NAMES,
            SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
        )
    )

    override val language = MutableStateFlow("")

    override fun updateUsingDarkTheme() {
        useDarkThemeChanged(
            isUsingDarkMode(
                resources = resources,
                powerManager = powerManager,
            )
        )
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
        theme.value = theme.value.copy(usingDynamicColors = use)
    }

    override fun useBlurChanged(use: Boolean) {
        theme.value = theme.value.copy(usingBlur = use)
    }

    override fun useMultiline(use: Boolean) {
        theme.value = theme.value.copy(multiline = use)
    }

    override fun setLongPollStateToApply(newState: LongPollState) {
        longPollStateToApply.update { newState }
        Log.d("UserSettings", "setLongPollState: $newState")
    }

    override fun updateLongPollCurrentState(currentState: LongPollState) {
        longPollCurrentState.update { currentState }
        Log.d("UserSettings", "updateLongPollCurrentState: $currentState")
    }

    override fun setOnline(use: Boolean) {
        online.value = use
    }

    override fun enableDebugSettings(enable: Boolean) {
        debugSettingsEnabled.update { enable }
    }

    override fun onUseContactNamesChanged(use: Boolean) {
        useContactNames.update { use }
    }

    override fun onLanguageChanged(newLanguage: String) {
        language.update { newLanguage }
    }
}
