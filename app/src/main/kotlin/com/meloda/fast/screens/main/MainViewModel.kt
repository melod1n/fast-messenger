package com.meloda.fast.screens.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val router: Router) : BaseViewModel() {

    fun checkSession() = viewModelScope.launch {
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
            sendEvent(StartServicesEvent)
            router.replaceScreen(Screens.Conversations())
        } else {
            router.replaceScreen(Screens.Login())
        }
    }

}

data class SetNavBarVisibilityEvent(val isVisible: Boolean) : VkEvent()

object StartServicesEvent : VkEvent()