package com.meloda.app.fast.settings.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.datastore.AppSettings

@Immutable
data class SettingsScreenState(
    val showOptions: SettingsShowOptions,
    val settings: List<UiItem>,
    val showDebugOptions: Boolean
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            showOptions = SettingsShowOptions.EMPTY,
            settings = emptyList(),
            showDebugOptions = AppSettings.Debug.showDebugCategory
        )
    }
}
