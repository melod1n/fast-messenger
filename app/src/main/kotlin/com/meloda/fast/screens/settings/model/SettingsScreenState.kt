package com.meloda.fast.screens.settings.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.ext.isDebugSettingsShown
import com.meloda.fast.screens.settings.HapticType

@Immutable
data class SettingsScreenState(
    val showOptions: SettingsShowOptions,
    val settings: List<SettingsItem<*>>,
    val useHaptics: HapticType,
    val isNeedToOpenUpdates: Boolean,
    val isNeedToRequestNotificationPermission: Boolean,
    val showDebugOptions: Boolean
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            showOptions = SettingsShowOptions.EMPTY,
            settings = emptyList(),
            useHaptics = HapticType.None,
            isNeedToOpenUpdates = false,
            isNeedToRequestNotificationPermission = false,
            showDebugOptions = isDebugSettingsShown()
        )
    }
}
