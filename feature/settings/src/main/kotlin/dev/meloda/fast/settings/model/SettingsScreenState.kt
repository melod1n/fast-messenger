package dev.meloda.fast.settings.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.datastore.AppSettings

@Immutable
data class SettingsScreenState(
    val settings: List<UiItem>,
    val showDebugOptions: Boolean
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            settings = emptyList(),
            showDebugOptions = AppSettings.Debug.showDebugCategory
        )
    }
}
