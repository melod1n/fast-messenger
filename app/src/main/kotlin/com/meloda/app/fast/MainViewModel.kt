package com.meloda.app.fast

import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.meloda.app.fast.auth.AuthGraph
import com.meloda.app.fast.common.LongPollController
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.ifEmpty
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.model.LongPollState
import com.meloda.app.fast.data.db.GetCurrentAccountUseCase
import com.meloda.app.fast.datastore.AppSettings
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.navigation.Main
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface MainViewModel {

    val startDestination: StateFlow<Any?>
    val isNeedToReplaceWithAuth: StateFlow<Boolean>

    val isNeedToShowNotificationsDeniedDialog: StateFlow<Boolean>
    val isNeedToShowNotificationsRationaleDialog: StateFlow<Boolean>
    val isNeedToCheckNotificationsPermission: StateFlow<Boolean>
    val isNeedToRequestNotifications: StateFlow<Boolean>

    fun onError(error: BaseError)

    fun onNavigatedToAuth()

    fun onAppResumed()

    @OptIn(ExperimentalPermissionsApi::class)
    fun onPermissionCheckStatus(status: PermissionStatus)
    fun onPermissionsRequested()

    fun onNotificationsDeniedDialogConfirmClicked()
    fun onNotificationsDeniedDialogCancelClicked()
    fun onNotificationsDeniedDialogDismissed()
    fun onNotificationsRationaleDialogDismissed()
    fun onNotificationsRationaleDialogCancelClicked()
}

class MainViewModelImpl(
    private val getCurrentAccountUseCase: GetCurrentAccountUseCase,
    private val userSettings: UserSettings,
    private val longPollController: LongPollController
) : MainViewModel, ViewModel() {

    init {
        loadAccounts()
    }

    override val startDestination = MutableStateFlow<Any?>(null)
    override val isNeedToReplaceWithAuth = MutableStateFlow(false)

    override val isNeedToShowNotificationsDeniedDialog = MutableStateFlow(false)
    override val isNeedToShowNotificationsRationaleDialog = MutableStateFlow(false)
    override val isNeedToCheckNotificationsPermission = MutableStateFlow(false)
    override val isNeedToRequestNotifications = MutableStateFlow(false)

    override fun onError(error: BaseError) {
        when (error) {
            BaseError.SessionExpired -> {
                isNeedToReplaceWithAuth.update { true }
            }
        }
    }

    override fun onNavigatedToAuth() {
        isNeedToReplaceWithAuth.update { false }
    }

    override fun onAppResumed() {
        if (isNeedToShowNotificationsRationaleDialog.value) {
            isNeedToShowNotificationsRationaleDialog.update { false }
            isNeedToCheckNotificationsPermission.update { true }
        }

        val newLanguage = AppCompatDelegate.getApplicationLocales()
            .toLanguageTags()
            .ifEmpty { null }
            ?: LocaleListCompat.getDefault()
                .toLanguageTags()
                .split(",")
                .firstOrNull()
                .orEmpty()
                .take(5)

        userSettings.onAppLanguageChanged(newLanguage)
    }

    @ExperimentalPermissionsApi
    override fun onPermissionCheckStatus(status: PermissionStatus) {
        isNeedToCheckNotificationsPermission.update { false }

        when (status) {
            is PermissionStatus.Denied -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

                if (status.shouldShowRationale) {
                    isNeedToShowNotificationsRationaleDialog.update { true }
                } else {
                    isNeedToShowNotificationsDeniedDialog.update { true }
                }
            }

            PermissionStatus.Granted -> {
                if (isNeedToShowNotificationsRationaleDialog.value) {
                    isNeedToShowNotificationsRationaleDialog.update { false }
                }
            }
        }
    }

    override fun onPermissionsRequested() {
        isNeedToRequestNotifications.update { false }
    }

    override fun onNotificationsDeniedDialogConfirmClicked() {
        isNeedToRequestNotifications.update { true }
    }

    override fun onNotificationsDeniedDialogCancelClicked() {
        isNeedToShowNotificationsDeniedDialog.update { false }
        disableBackgroundLongPoll()
    }

    override fun onNotificationsDeniedDialogDismissed() {
        isNeedToShowNotificationsDeniedDialog.update { false }
    }

    override fun onNotificationsRationaleDialogDismissed() {
        isNeedToShowNotificationsRationaleDialog.update { false }
    }

    override fun onNotificationsRationaleDialogCancelClicked() {
        isNeedToShowNotificationsRationaleDialog.update { false }
        disableBackgroundLongPoll()
    }

    private fun listenLongPollState() {
        longPollController.stateToApply.listenValue { newState ->
            if (newState == LongPollState.Background) {
                isNeedToCheckNotificationsPermission.update { true }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAccount = getCurrentAccountUseCase()

            Log.d("MainViewModel", "currentAccount: $currentAccount")

            listenLongPollState()

            if (currentAccount != null) {
                UserConfig.apply {
                    this.userId = currentAccount.userId
                    this.accessToken = currentAccount.accessToken
                    this.fastToken = currentAccount.fastToken
                    this.trustedHash = currentAccount.trustedHash
                }

                longPollController.setStateToApply(
                    if (AppSettings.Debug.longPollInBackground) {
                        LongPollState.Background
                    } else {
                        LongPollState.InApp
                    }
                )
            }

            startDestination.setValue {
                if (currentAccount == null) AuthGraph
                else Main
            }
        }
    }

    private fun disableBackgroundLongPoll() {
        AppSettings.Debug.longPollInBackground = false
        longPollController.setStateToApply(LongPollState.InApp)
    }
}
