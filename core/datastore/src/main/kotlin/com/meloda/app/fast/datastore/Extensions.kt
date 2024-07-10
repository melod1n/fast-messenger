package com.meloda.app.fast.datastore

import android.content.res.Configuration
import android.content.res.Resources
import android.os.PowerManager
import androidx.appcompat.app.AppCompatDelegate

fun isUsingDarkMode(
    resources: Resources,
    powerManager: PowerManager,
): Boolean {
    val nightThemeMode: Int = SettingsController.getInt(
        SettingsKeys.KEY_APPEARANCE_DARK_THEME,
        SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
    )

    val appForceDarkMode = nightThemeMode == AppCompatDelegate.MODE_NIGHT_YES
    val appBatterySaver = nightThemeMode == AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

    val systemUiNightMode = resources.configuration.uiMode

    val isSystemBatterySaver = powerManager.isPowerSaveMode
    val isSystemUsingDarkTheme =
        systemUiNightMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    return appForceDarkMode || (appBatterySaver && isSystemBatterySaver) || (!appBatterySaver && isSystemUsingDarkTheme && nightThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
}

fun isUsingDynamicColors(): Boolean = SettingsController.getBoolean(
    SettingsKeys.KEY_USE_DYNAMIC_COLORS,
    SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
)

fun isUsingAmoledBackground(): Boolean = SettingsController.getBoolean(
    SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
    SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME
)

fun selectedColorScheme(): Int = SettingsController.getInt(
    SettingsKeys.KEY_APPEARANCE_COLOR_SCHEME,
    SettingsKeys.DEFAULT_VALUE_APPEARANCE_COLOR_SCHEME
)

fun isUsingBlur(): Boolean = SettingsController.getBoolean(
    SettingsKeys.KEY_APPEARANCE_BLUR,
    SettingsKeys.DEFAULT_VALUE_KEY_APPEARANCE_BLUR
)

fun isDebugSettingsShown(): Boolean = SettingsController.getBoolean(
    SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
    false
)

fun isMultiline(): Boolean = SettingsController.getBoolean(
    SettingsKeys.KEY_APPEARANCE_MULTILINE,
    SettingsKeys.DEFAULT_VALUE_MULTILINE
)
