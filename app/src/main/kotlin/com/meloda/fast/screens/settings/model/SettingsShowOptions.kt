package com.meloda.fast.screens.settings.model

data class SettingsShowOptions(
    val showLogOut: Boolean,
    val showPerformCrash: Boolean
) {

    companion object {
        val EMPTY: SettingsShowOptions = SettingsShowOptions(
            showLogOut = false,
            showPerformCrash = false
        )
    }
}
