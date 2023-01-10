package com.meloda.fast.screens.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val router: Router) : BaseViewModel() {

    val servicesState = MutableStateFlow<ServicesState>(ServicesState.Unknown)

    fun checkSession() {
        viewModelScope.launch {
            val currentUserId = UserConfig.currentUserId
            val userId = UserConfig.userId
            val accessToken = UserConfig.accessToken
            val fastToken = UserConfig.fastToken

            Log.d(
                "MainViewModel",
                "checkSession: currentUserId: $currentUserId; userId: $userId; accessToken: $accessToken; fastToken: $fastToken"
            )

            if (UserConfig.isLoggedIn()) {
                servicesState.emit(ServicesState.Started)
                openScreen(Screens.Conversations())
            } else {
                servicesState.emit(ServicesState.Stopped)
                openScreen(Screens.Login())
            }
        }
    }

    private fun openScreen(screen: Screen) {
        router.replaceScreen(screen)
    }
}
