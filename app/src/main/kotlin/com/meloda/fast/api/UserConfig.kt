package com.meloda.fast.api

import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.model.AppAccount

object UserConfig {

    private const val ARG_CURRENT_USER_ID = "current_user_id"

    const val FAST_APP_ID = "6964679"

    private val preferences get() = AppGlobal.preferences

    var currentUserId: Int = -1
        get() = preferences.getInt(ARG_CURRENT_USER_ID, -1)
        set(value) {
            field = value
            preferences.edit { putInt(ARG_CURRENT_USER_ID, value) }
        }

    var userId: Int = -1
        private set
    var accessToken: String = ""
        private set
    var fastToken: String? = ""
        private set

    fun parse(account: AppAccount) {
        this.userId = account.userId
        this.accessToken = account.accessToken
        this.fastToken = account.fastToken
    }

    fun clear() {
        currentUserId = -1
        accessToken = ""
        fastToken = ""
        userId = -1
    }

    fun isLoggedIn(): Boolean {
        return currentUserId > 0 && userId > 0 && accessToken.isNotBlank()
    }

    val vkUser = MutableLiveData<VkUser?>(null)

}