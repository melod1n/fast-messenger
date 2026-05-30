package dev.meloda.fast.settings.model

sealed interface SettingsEffect {
    data class Navigate(val intent: SettingsNavigationIntent) : SettingsEffect
    data class PerformHaptic(val type: HapticType) : SettingsEffect
}
