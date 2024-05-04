package com.meloda.fast.modules.auth.screens.logo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.BuildConfig
import com.meloda.fast.api.UserConfig
import com.meloda.fast.database.dao.AccountsDao
import com.meloda.fast.ext.setValue
import com.meloda.fast.model.AppAccount
import com.meloda.fast.modules.auth.screens.logo.model.LogoScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface LogoViewModel {
    val screenState: StateFlow<LogoScreenState>
    fun onLogoLongClicked()
    fun onRestarted()
}

class LogoViewModelImpl(
    private val accountsDao: AccountsDao
) : LogoViewModel, ViewModel() {

    override val screenState = MutableStateFlow(LogoScreenState.EMPTY)

    override fun onLogoLongClicked() {
        val currentAccount = AppAccount(
            userId = BuildConfig.debugUserId.toInt(),
            accessToken = BuildConfig.debugAccessToken,
            fastToken = null,
            trustedHash = null
        ).also { account ->
            UserConfig.currentUserId = account.userId
            UserConfig.userId = account.userId
            UserConfig.accessToken = account.accessToken
            UserConfig.fastToken = account.fastToken
            UserConfig.trustedHash = account.trustedHash
        }

        viewModelScope.launch(Dispatchers.IO) {
            accountsDao.insertAll(listOf(currentAccount))

            delay(350)
            screenState.setValue { old -> old.copy(isNeedToRestart = true) }
        }
    }

    override fun onRestarted() {
        screenState.setValue { old -> old.copy(isNeedToRestart = false) }
    }
}
