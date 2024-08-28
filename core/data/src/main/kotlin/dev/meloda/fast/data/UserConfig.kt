package dev.meloda.fast.data

import dev.meloda.fast.datastore.AppSettings

object UserConfig {

    private const val ARG_CURRENT_USER_ID = "current_user_id"

    var currentUserId: Int = -1
        get() = AppSettings.getInt(ARG_CURRENT_USER_ID, -1)
        set(value) {
            field = value
            AppSettings.edit { putInt(ARG_CURRENT_USER_ID, value) }
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