package com.meloda.fast.api

import androidx.lifecycle.MutableLiveData
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.common.AppGlobal

object UserConfig {

    private const val TOKEN = "token"
    private const val USER_ID = "user_id"

    const val FAST_APP_ID = "6964679"

    var userId: Int = -1
        get() = AppGlobal.preferences.getInt(USER_ID, -1)
        set(value) {
            field = value
            AppGlobal.preferences.edit().putInt(USER_ID, value).apply()
        }

    var accessToken: String = ""
        get() = AppGlobal.preferences.getString(TOKEN, "") ?: ""
        set(value) {
            field = value
            AppGlobal.preferences.edit().putString(TOKEN, value).apply()
        }

    fun clear() {
        accessToken = ""
        userId = -1
    }

    fun isLoggedIn() = userId > 0 && accessToken.isNotBlank()

    val vkUser = MutableLiveData<VkUser?>(null)

}