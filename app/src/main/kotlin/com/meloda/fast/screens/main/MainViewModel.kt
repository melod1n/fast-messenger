package com.meloda.fast.screens.main

import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.UserConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.account.AccountsDao
import com.meloda.fast.ext.setValue
import com.meloda.fast.ext.updateValue
import com.meloda.fast.screens.main.model.LongPollState
import com.meloda.fast.screens.main.model.MainScreenState
import com.meloda.fast.screens.settings.SettingsKeys
import com.meloda.fast.screens.settings.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface MainViewModel {

    val screenState: StateFlow<MainScreenState>

    val longPollState: StateFlow<LongPollState>
    val startOnlineService: StateFlow<Boolean>

    fun useDynamicColorsChanged(use: Boolean)

    fun useDarkThemeChanged(use: Boolean)

    fun onRequestNotificationsPermissionClicked(fromRationale: Boolean)
    fun onNotificationsAlertNegativeClicked()

    fun onNotificationsRequested()

    fun onAppPermissionsOpened()
}

class MainViewModelImpl(
    private val accountsDao: AccountsDao,
    private val userSettings: UserSettings
) : MainViewModel, ViewModel() {

    init {
        loadAccounts()
    }

    override val screenState = MutableStateFlow(MainScreenState.EMPTY)

    override val longPollState = MutableStateFlow(
        if (AppGlobal.preferences.getBoolean(
                SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                SettingsKeys.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
            )
        ) {
            LongPollState.ForegroundService
        } else {
            LongPollState.DefaultService
        }
    )
    override val startOnlineService = MutableStateFlow(
        AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_VISIBILITY_SEND_ONLINE_STATUS,
            SettingsKeys.DEFAULT_VALUE_KEY_VISIBILITY_SEND_ONLINE_STATUS
        )
    )

    override fun useDynamicColorsChanged(use: Boolean) {
        screenState.updateValue(screenState.value.copy(useDynamicColors = use))
    }

    override fun useDarkThemeChanged(use: Boolean) {
        screenState.updateValue(screenState.value.copy(useDarkTheme = use))
    }

    override fun onRequestNotificationsPermissionClicked(fromRationale: Boolean) {
        screenState.setValue { old ->
            if (fromRationale) {
                old.copy(openAppPermissions = true)
            } else {
                old.copy(requestNotifications = true)
            }
        }
    }

    override fun onNotificationsAlertNegativeClicked() {
        AppGlobal.preferences.edit {
            putBoolean(
                SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                false
            )
        }
        userSettings.setLongPollBackground(false)
    }

    override fun onNotificationsRequested() {
        screenState.setValue { old -> old.copy(requestNotifications = false) }
    }

    override fun onAppPermissionsOpened() {
        screenState.setValue { old -> old.copy(openAppPermissions = false) }
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
