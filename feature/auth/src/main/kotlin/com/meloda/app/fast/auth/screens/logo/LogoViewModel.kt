package com.meloda.app.fast.auth.screens.logo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.auth.BuildConfig
import com.meloda.app.fast.auth.screens.logo.model.LogoScreenState
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.model.database.AccountEntity
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
    private val accountsRepository: AccountsRepository
) : LogoViewModel, ViewModel() {

    override val screenState = MutableStateFlow(LogoScreenState.EMPTY)

    override fun onLogoLongClicked() {
        val currentAccount = AccountEntity(
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
            accountsRepository.storeAccounts(listOf(currentAccount))

            delay(350)
            screenState.setValue { old -> old.copy(isNeedToRestart = true) }
        }
    }

    override fun onRestarted() {
        screenState.setValue { old -> old.copy(isNeedToRestart = false) }
    }
}
