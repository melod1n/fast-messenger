package com.meloda.app.fast.datastore

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.Delegates

object SettingsController {

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

    var isLongPollInBackgroundEnabled: Boolean =
        SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        get() = getBoolean(
            SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
            SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
        )
        set(value) {
            field = value
            put(SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND, value)
        }
}
