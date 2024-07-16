package com.meloda.app.fast.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import com.meloda.app.fast.common.model.DarkMode
import kotlin.properties.Delegates
import kotlin.reflect.KClass

object AppSettings {

    private var preferences: SharedPreferences by Delegates.notNull()

    fun init(preferences: SharedPreferences) {
        this.preferences = preferences
    }

    fun edit(
        commit: Boolean = false,
        action: SharedPreferences.Editor.() -> Unit
    ) {
        preferences.edit(commit, action)
    }

    fun getString(key: String, defaultValue: String?): String? {
        return preferences.getString(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return preferences.getFloat(key, defaultValue)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(clazz: KClass<T>, key: String, defaultValue: T): T {
        return when (clazz) {
            String::class -> getString(key, defaultValue as String)
            Boolean::class -> getBoolean(key, defaultValue as Boolean)
            Int::class -> getInt(key, defaultValue as Int)
            Long::class -> getLong(key, defaultValue as Long)
            Float::class -> getFloat(key, defaultValue as Float)
            else -> throw IllegalStateException("Unsupported class: $clazz")
        } as T
    }

    inline fun <reified T> get(key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> getString(key, defaultValue as String)
            Boolean::class -> getBoolean(key, defaultValue as Boolean)
            Int::class -> getInt(key, defaultValue as Int)
            Long::class -> getLong(key, defaultValue as Long)
            Float::class -> getFloat(key, defaultValue as Float)
            else -> throw IllegalStateException("Unsupported class: ${T::class}")
        } as T
    }

    fun <T> put(key: String, newValue: T?) {
        preferences.edit {
            when (newValue) {
                is String -> putString(key, newValue)
                is Boolean -> putBoolean(key, newValue)
                is Int -> putInt(key, newValue)
                is Long -> putLong(key, newValue)
                is Float -> putFloat(key, newValue)
            }
        }
    }

    var deviceId: String
        get() = get("device_id", "")
        set(value) = put("device_id", value)

    object General {
        var useContactNames: Boolean
            get() = get(
                SettingsKeys.KEY_USE_CONTACT_NAMES,
                SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
            )
            set(value) = put(SettingsKeys.KEY_USE_CONTACT_NAMES, value)

        var enablePullToRefresh: Boolean
            get() = get(
                SettingsKeys.KEY_ENABLE_PULL_TO_REFRESH,
                SettingsKeys.DEFAULT_VALUE_ENABLE_PULL_TO_REFRESH
            )
            set(value) = put(SettingsKeys.KEY_ENABLE_PULL_TO_REFRESH, value)
    }

    object Appearance {
        var enableMultiline: Boolean
            get() = get(
                SettingsKeys.KEY_APPEARANCE_MULTILINE,
                SettingsKeys.DEFAULT_VALUE_MULTILINE
            )
            set(value) = put(SettingsKeys.KEY_APPEARANCE_MULTILINE, value)

        var darkMode: DarkMode
            get() = get(
                SettingsKeys.KEY_APPEARANCE_DARK_MODE,
                SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_MODE
            ).let(DarkMode.Companion::parse)
            set(mode) = put(SettingsKeys.KEY_APPEARANCE_DARK_MODE, mode.value)

        var enableAmoledDark: Boolean
            get() = get(
                SettingsKeys.KEY_APPEARANCE_AMOLED_THEME,
                SettingsKeys.DEFAULT_VALUE_APPEARANCE_AMOLED_THEME
            )
            set(value) = put(SettingsKeys.KEY_APPEARANCE_AMOLED_THEME, value)

        var enableDynamicColors: Boolean
            get() = get(
                SettingsKeys.KEY_USE_DYNAMIC_COLORS,
                SettingsKeys.DEFAULT_VALUE_USE_DYNAMIC_COLORS
            )
            set(value) = put(SettingsKeys.KEY_USE_DYNAMIC_COLORS, value)

        var appLanguage: String
            get() = get(
                SettingsKeys.KEY_APPEARANCE_LANGUAGE,
                SettingsKeys.DEFAULT_APPEARANCE_LANGUAGE
            )
            set(value) = put(SettingsKeys.KEY_APPEARANCE_LANGUAGE, value)
    }

    object Features {
        var fastText: String
            get() = get(
                SettingsKeys.KEY_FEATURES_FAST_TEXT,
                SettingsKeys.DEFAULT_VALUE_FEATURES_FAST_TEXT
            )
            set(value) = put(SettingsKeys.KEY_FEATURES_FAST_TEXT, value)
    }

    object Activity {
        var sendOnlineStatus: Boolean
            get() = get(
                SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS,
                SettingsKeys.DEFAULT_VALUE_KEY_ACTIVITY_SEND_ONLINE_STATUS
            )
            set(value) = put(SettingsKeys.KEY_ACTIVITY_SEND_ONLINE_STATUS, value)
    }

    object Debug {
        var showAlertAfterCrash: Boolean
            get() = get(
                SettingsKeys.KEY_DEBUG_SHOW_CRASH_ALERT,
                true
            )
            set(value) = put(SettingsKeys.KEY_DEBUG_SHOW_CRASH_ALERT, value)

        var longPollInBackground: Boolean
            get() = get(
                SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
            )
            set(value) = put(SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND, value)

        var useBlur: Boolean
            get() = get(
                SettingsKeys.KEY_APPEARANCE_USE_BLUR,
                SettingsKeys.DEFAULT_VALUE_KEY_APPEARANCE_USE_BLUR
            )
            set(value) = put(SettingsKeys.KEY_APPEARANCE_USE_BLUR, value)

        var showEmojiButton: Boolean
            get() = get(
                SettingsKeys.KEY_SHOW_EMOJI_BUTTON,
                SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
            )
            set(value) = put(SettingsKeys.KEY_SHOW_EMOJI_BUTTON, value)

        var showTimeInActionMessages: Boolean
            get() = get(
                SettingsKeys.KEY_APPEARANCE_SHOW_TIME_IN_ACTION_MESSAGES,
                SettingsKeys.DEFAULT_VALUE_APPEARANCE_SHOW_TIME_IN_ACTION_MESSAGES
            )
            set(value) = put(SettingsKeys.KEY_APPEARANCE_SHOW_TIME_IN_ACTION_MESSAGES, value)

        var showDebugCategory: Boolean
            get() = get(
                SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
                false
            )
            set(value) = put(SettingsKeys.KEY_SHOW_DEBUG_CATEGORY, value)
    }
}
