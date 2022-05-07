package com.meloda.fast.screens.main

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.service.MessagesUpdateService
import com.meloda.fast.service.OnlineService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val router: Router) : BaseViewModel() {

    fun checkSession(context: Context) {
        val currentUserId = UserConfig.currentUserId
        val userId = UserConfig.userId
        val accessToken = UserConfig.accessToken

        viewModelScope.launch {
            sendEvent(SetNavBarVisibilityEvent(UserConfig.isLoggedIn()))
        }

        Log.d(
            "MainViewModel",
            "checkSession: currentUserId: $currentUserId; userId: $userId; accessToken: $accessToken"
        )
        if (UserConfig.isLoggedIn()) {
            router.replaceScreen(Screens.Conversations())

            context.run {
                startService(Intent(this, MessagesUpdateService::class.java))
                startService(Intent(this, OnlineService::class.java))
            }
        } else {
            router.replaceScreen(Screens.Login())
        }
    }

}

data class SetNavBarVisibilityEvent(val isVisible: Boolean) : VkEvent()