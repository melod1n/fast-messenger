package dev.meloda.fast.settings.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class SettingsDialog {
    data object LogOut : SettingsDialog()
    data object PerformCrash : SettingsDialog()
    data object ImportAuthData : SettingsDialog()

    data class ExportAuthData(
        val accessToken: String,
        val exchangeToken: String?,
        val trustedHash: String?
    ) : SettingsDialog()
}
