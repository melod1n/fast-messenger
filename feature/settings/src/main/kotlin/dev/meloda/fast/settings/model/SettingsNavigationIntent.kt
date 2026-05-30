package dev.meloda.fast.settings.model

sealed class SettingsNavigationIntent {
    data object Back : SettingsNavigationIntent()
    data object Language : SettingsNavigationIntent()
    data object Restart : SettingsNavigationIntent()
    data object LogOut : SettingsNavigationIntent()
}
