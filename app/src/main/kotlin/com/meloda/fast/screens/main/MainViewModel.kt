package com.meloda.fast.screens.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.Screen
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.DeprecatedBaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.screens.main.activity.ServicesState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface MainViewModel {
    val events: Flow<VkEvent>

    val servicesState: Flow<ServicesState>
}

class MainViewModelImpl constructor(
    private val router: Router
) : MainViewModel, DeprecatedBaseViewModel() {

    override val events = tasksEvent.map { it }

    override val servicesState = MutableStateFlow<ServicesState>(ServicesState.Unknown)

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val currentUserId = UserConfig.currentUserId
            val userId = UserConfig.userId
            val accessToken = UserConfig.accessToken
            val fastToken = UserConfig.fastToken

            Log.d(
                "MainViewModel",
                "checkSession: currentUserId: $currentUserId; userId: $userId; accessToken: $accessToken; fastToken: $fastToken"
            )

            // TODO: 14.08.2023, Danil Nikolaev: rewrite
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
