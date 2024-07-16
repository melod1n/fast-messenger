package dev.meloda.fast.common

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.Delegates

object UserConfig {

    private const val ARG_CURRENT_USER_ID = "current_user_id"

    private var preferences: SharedPreferences by Delegates.notNull()

    fun init(preferences: SharedPreferences) {
        this.preferences = preferences
    }

    var currentUserId: Int = -1
        get() = preferences.getInt(ARG_CURRENT_USER_ID, -1)
        set(value) {
            field = value
            preferences.edit { putInt(ARG_CURRENT_USER_ID, value) }
        }

    var userId: Int = -1
    var accessToken: String = ""
    var fastToken: String? = ""
    var trustedHash: String? = null

    fun clear() {
        currentUserId = -1
        accessToken = ""
        fastToken = ""
        userId = -1
    }

    fun isLoggedIn(): Boolean {
        return currentUserId > 0 && userId > 0 && accessToken.isNotBlank()
    }
}
