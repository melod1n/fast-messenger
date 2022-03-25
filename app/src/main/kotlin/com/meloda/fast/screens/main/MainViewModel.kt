package com.meloda.fast.screens.main

import android.content.Context
import android.content.Intent
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.Screens
import com.meloda.fast.service.MessagesUpdateService
import com.meloda.fast.service.OnlineService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val router: Router) : BaseViewModel() {

    fun checkSession(context: Context) {
        if (UserConfig.isLoggedIn()) {
            router.navigateTo(Screens.Conversations())

            context.run {
                startService(Intent(this, MessagesUpdateService::class.java))
                startService(Intent(this, OnlineService::class.java))
            }
        } else {
            router.navigateTo(Screens.Login())
        }
    }

}