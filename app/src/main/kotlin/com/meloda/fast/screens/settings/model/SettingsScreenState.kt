package com.meloda.fast.screens.settings.model

import com.meloda.fast.screens.settings.HapticType
import com.meloda.fast.screens.settings.SettingsKeys

data class SettingsScreenState(
    val showOptions: SettingsShowOptions,
    val multilineEnabled: Boolean,
    val useDynamicColors: Boolean,
    val settings: List<SettingsItem<*>>,
    val useHaptics: HapticType,
    val isNeedToOpenUpdates: Boolean,
    val isNeedToRequestNotificationPermission: Boolean
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            showOptions = SettingsShowOptions.EMPTY,
            multilineEnabled = SettingsKeys.DEFAULT_VALUE_MULTILINE,
            useDynamicColors = SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS,
            settings = emptyList(),
            useHaptics = HapticType.None,
            isNeedToOpenUpdates = false,
            isNeedToRequestNotificationPermission = false
        )
    }
}
