package dev.meloda.fast.datastore

import androidx.appcompat.app.AppCompatDelegate

object SettingsKeys {
    const val KEY_ACCOUNT = "account"
    const val KEY_ACCOUNT_LOGOUT = "account_logout"

    const val KEY_GENERAL = "general"
    const val KEY_USE_CONTACT_NAMES = "general_use_contact_names"
    const val DEFAULT_VALUE_USE_CONTACT_NAMES = false
    const val KEY_ENABLE_PULL_TO_REFRESH = "general_pull_to_refresh"
    const val DEFAULT_VALUE_ENABLE_PULL_TO_REFRESH = false
    const val KEY_SHOW_EMOJI_BUTTON = "general_show_emoji_button"
    const val DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON = false

    const val KEY_APPEARANCE = "appearance"
    const val KEY_APPEARANCE_MULTILINE = "appearance_multiline"
    const val DEFAULT_VALUE_MULTILINE = true
    const val KEY_APPEARANCE_DARK_MODE = "appearance_appearance_dark_mode"
    const val DEFAULT_VALUE_APPEARANCE_DARK_MODE = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    const val KEY_APPEARANCE_AMOLED_THEME = "appearance_amoled_theme"
    const val DEFAULT_VALUE_APPEARANCE_AMOLED_THEME = false
    const val KEY_USE_DYNAMIC_COLORS = "appearance_use_dynamic_colors"
    const val DEFAULT_VALUE_USE_DYNAMIC_COLORS = false
    const val KEY_APPEARANCE_COLOR_SCHEME = "appearance_color_scheme"
    const val DEFAULT_VALUE_APPEARANCE_COLOR_SCHEME = 0
    const val KEY_APPEARANCE_LANGUAGE = "appearance_language"
    const val DEFAULT_APPEARANCE_LANGUAGE = ""
    const val KEY_APPEARANCE_USE_BLUR = "appearance_use_blur"
    const val DEFAULT_VALUE_KEY_APPEARANCE_USE_BLUR = false
    const val KEY_APPEARANCE_SHOW_TIME_IN_ACTION_MESSAGES =
        "appearance_show_time_in_action_messages"
    const val DEFAULT_VALUE_APPEARANCE_SHOW_TIME_IN_ACTION_MESSAGES = false

    const val KEY_FEATURES_FAST_TEXT = "features_fast_text"
    const val DEFAULT_VALUE_FEATURES_FAST_TEXT = "¯\\_(ツ)_/¯"
    const val KEY_FEATURES_LONG_POLL_IN_BACKGROUND = "features_lp_background"
    const val DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND = false

    const val KEY_ACTIVITY_SEND_ONLINE_STATUS = "activity_send_online_status"
    const val DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS = false

    const val KEY_DEBUG_PERFORM_CRASH = "debug_perform_crash"
    const val KEY_DEBUG_SHOW_CRASH_ALERT = "debug_show_crash_alert"
    const val KEY_DEBUG_HIDE_DEBUG_LIST = "debug_hide_debug_list"
    const val KEY_ENABLE_ANIMATIONS_IN_MESSAGES = "debug_enable_animations_in_messages"

    const val KEY_SHOW_DEBUG_CATEGORY = "show_debug_category"

    const val ID_DMITRY = 37610580
}
