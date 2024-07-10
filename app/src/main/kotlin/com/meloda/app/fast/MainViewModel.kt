package com.meloda.app.fast

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.extensions.updateValue
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.datastore.SettingsController
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.model.LongPollState
import com.meloda.app.fast.model.MainScreenState
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

    fun onError(error: BaseError)

    fun onAuthOpened()
}

class MainViewModelImpl(
    private val accountsRepository: AccountsRepository,
    private val userSettings: UserSettings
) : MainViewModel, ViewModel() {

    init {
        loadAccounts()
    }

    override val screenState = MutableStateFlow(MainScreenState.EMPTY)

    override val longPollState = MutableStateFlow(
        if (SettingsController.getBoolean(
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
        SettingsController.getBoolean(
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
                old.copy(isNeedToOpenAppPermissions = true)
            } else {
                old.copy(isNeedToRequestNotifications = true)
            }
        }
    }

    override fun onNotificationsAlertNegativeClicked() {
        SettingsController.edit {
            putBoolean(
                SettingsKeys.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                false
            )
        }
        userSettings.setLongPollBackground(false)
    }

    override fun onNotificationsRequested() {
        screenState.setValue { old -> old.copy(isNeedToRequestNotifications = false) }
    }

    override fun onAppPermissionsOpened() {
        screenState.setValue { old -> old.copy(isNeedToOpenAppPermissions = false) }
    }

    override fun onError(error: BaseError) {
        when (error) {
            BaseError.SessionExpired -> {
                screenState.setValue { old -> old.copy(isNeedToOpenAuth = true) }
            }
        }
    }

    override fun onAuthOpened() {
        screenState.setValue { old -> old.copy(isNeedToOpenAuth = false) }
    }

    private fun loadAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = accountsRepository.getAccounts()

            Log.d("MainViewModel", "initUserConfig: accounts: $accounts")

            if (accounts.isNotEmpty()) {
                val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                if (currentAccount != null) {
                    UserConfig.apply {
                        this.userId = currentAccount.userId
                        this.accessToken = currentAccount.accessToken
                        this.fastToken = currentAccount.fastToken
                        this.trustedHash = currentAccount.trustedHash
                    }
                }
            }

            screenState.setValue { old ->
                old.copy(
                    accounts = accounts,
                    accountsLoaded = true
                )
            }
        }
    }
}
