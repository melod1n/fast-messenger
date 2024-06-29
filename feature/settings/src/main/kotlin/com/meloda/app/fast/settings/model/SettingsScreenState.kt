package com.meloda.app.fast.settings.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.datastore.isDebugSettingsShown
import com.meloda.app.fast.settings.HapticType

@Immutable
data class SettingsScreenState(
    val showOptions: SettingsShowOptions,
    val settings: List<SettingsItem<*>>,
    val useHaptics: HapticType,
    val isNeedToRequestNotificationPermission: Boolean,
    val showDebugOptions: Boolean
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            showOptions = SettingsShowOptions.EMPTY,
            settings = emptyList(),
            useHaptics = HapticType.None,
            isNeedToRequestNotificationPermission = false,
            showDebugOptions = isDebugSettingsShown()
        )
    }
}
