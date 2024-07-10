package com.meloda.app.fast.settings.model

data class SettingsShowOptions(
    val showLogOut: Boolean,
    val showPerformCrash: Boolean,
    val showLongPollNotifications: Boolean
) {

    companion object {
        val EMPTY: SettingsShowOptions = SettingsShowOptions(
            showLogOut = false,
            showPerformCrash = false,
            showLongPollNotifications = false
        )
    }
}
