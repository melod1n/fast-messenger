package dev.meloda.fast.settings.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.datastore.AppSettings

@Immutable
data class SettingsScreenState(
    val settings: List<UiItem>,
    val dialog: SettingsDialog?
) {

    companion object {
        val EMPTY: SettingsScreenState = SettingsScreenState(
            settings = emptyList(),
            dialog = null
        )
    }
}
