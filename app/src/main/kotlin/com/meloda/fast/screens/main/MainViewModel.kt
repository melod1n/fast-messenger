package com.meloda.fast.screens.main

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.ext.updateValue
import com.meloda.fast.screens.main.model.MainScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MainViewModel {

    val screenState: StateFlow<MainScreenState>

    fun useDynamicColorsChanged(use: Boolean)

    fun useDarkThemeChanged(use: Boolean)
}

class MainViewModelImpl(
    private val accountsDao: AccountsDao
) : MainViewModel, BaseViewModel() {

    init {
        loadAccounts()
    }

    override val screenState = MutableStateFlow(MainScreenState.EMPTY)

    override fun useDynamicColorsChanged(use: Boolean) {
        screenState.updateValue(screenState.value.copy(useDynamicColors = use))
    }

    override fun useDarkThemeChanged(use: Boolean) {
        screenState.updateValue(screenState.value.copy(useDarkTheme = use))
    }

    private fun loadAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = accountsDao.getAll()

            Log.d("MainViewModel", "initUserConfig: accounts: $accounts")

            if (accounts.isNotEmpty()) {
                val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                if (currentAccount != null) {
                    UserConfig.parse(currentAccount)
                }
            }

            screenState.emit(
                screenState.value.copy(
                    accounts = accounts,
                    accountsLoaded = true
                )
            )
        }
    }
}
