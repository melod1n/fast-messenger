package com.meloda.fast.screens.settings.model

import com.meloda.fast.screens.settings.HapticType
import com.meloda.fast.screens.settings.presentation.SettingsFragment

data class SettingsScreenState(
    val showOptions: SettingsShowOptions,
    val useLargeTopAppBar: Boolean,
    val multilineEnabled: Boolean,
    val useDynamicColors: Boolean,
    val settings: List<SettingsItem<*>>,
    val useHaptics: HapticType
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            showOptions = SettingsShowOptions.EMPTY,
            useLargeTopAppBar = SettingsFragment.DEFAULT_VALUE_USE_LARGE_TOP_APP_BAR,
            multilineEnabled = SettingsFragment.DEFAULT_VALUE_MULTILINE,
            useDynamicColors = SettingsFragment.DEFAULT_VALUE_USE_DYNAMIC_COLORS,
            settings = emptyList(),
            useHaptics = HapticType.None
        )
    }
}
