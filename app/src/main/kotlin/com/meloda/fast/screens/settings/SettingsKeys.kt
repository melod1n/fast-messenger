package com.meloda.fast.screens.settings

import androidx.appcompat.app.AppCompatDelegate

object SettingsKeys {
    const val KEY_ACCOUNT = "account"
    const val KEY_ACCOUNT_LOGOUT = "account_logout"

    const val KEY_APPEARANCE = "appearance"
    const val KEY_APPEARANCE_MULTILINE = "appearance_multiline"
    const val DEFAULT_VALUE_MULTILINE = true
    const val KEY_APPEARANCE_DARK_THEME = "appearance_appearance_dark_theme"
    const val DEFAULT_VALUE_APPEARANCE_DARK_THEME = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    const val KEY_USE_DYNAMIC_COLORS = "appearance_use_dynamic_colors"
    const val DEFAULT_VALUE_USE_DYNAMIC_COLORS = false

    const val KEY_FEATURES_HIDE_KEYBOARD_ON_SCROLL = "features_hide_keyboard_on_scroll"
    const val KEY_FEATURES_FAST_TEXT = "features_fast_text"
    const val DEFAULT_VALUE_FEATURES_FAST_TEXT = "¯\\_(ツ)_/¯"
    const val KEY_FEATURES_LONG_POLL_IN_BACKGROUND = "features_lp_background"
    const val DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND = false

    const val KEY_VISIBILITY_SEND_ONLINE_STATUS = "visibility_send_online_status"

    const val KEY_UPDATES_CHECK_AT_STARTUP = "updates_check_at_startup"
    const val KEY_UPDATES_CHECK_UPDATES = "updates_check_updates"

    const val KEY_MS_APPCENTER_ENABLE = "msappcenter.enable"
    const val KEY_MS_APPCENTER_ENABLE_ON_DEBUG = "msappcenter.enable_on_debug"

    const val KEY_DEBUG_PERFORM_CRASH = "debug_perform_crash"

    const val KEY_DEBUG_SHOW_CRASH_ALERT = "debug_show_crash_alert"

    const val KEY_DEBUG_HIDE_DEBUG_LIST = "debug_hide_debug_list"

    const val KEY_SHOW_EXACT_TIME_ON_TIME_STAMP = "wip_show_exact_time_on_time_stamp"

    const val KEY_SHOW_DEBUG_CATEGORY = "show_debug_category"

    const val ID_DMITRY = 37610580
}
