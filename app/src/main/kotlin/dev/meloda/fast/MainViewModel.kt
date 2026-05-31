package dev.meloda.fast

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conena.nanokt.android.os.isMinSdk
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import dev.meloda.fast.auth.AuthGraph
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.extensions.ifEmpty
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.model.LongPollState
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.GetCurrentAccountUseCase
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.navigation.Main
import dev.meloda.fast.settings.navigation.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val getCurrentAccountUseCase: GetCurrentAccountUseCase,
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val userSettings: UserSettings,
    private val longPollController: LongPollController,
    private val logger: FastLogger
) : ViewModel() {

    val startDestination = MutableStateFlow<Any?>(null)
    val isNeedToReplaceWithAuth = MutableStateFlow(false)
    val currentUser = MutableStateFlow<VkUser?>(null)

    val isNeedToShowNotificationsDeniedDialog = MutableStateFlow(false)
    val isNeedToShowNotificationsRationaleDialog = MutableStateFlow(false)
    val isNeedToCheckNotificationsPermission = MutableStateFlow(false)
    val isNeedToRequestNotifications = MutableStateFlow(false)

    private var openNotificationsSettings = false
    private var openAppSettings = false

    fun onError(error: BaseError) {
        when (error) {
            BaseError.SessionExpired,
            BaseError.AccountBlocked -> {
                isNeedToReplaceWithAuth.update { true }
            }

            else -> Unit // TODO: 21-Mar-25, Danil Nikolaev: show error in ui
        }
    }

    fun onNavigatedToAuth() {
        isNeedToReplaceWithAuth.update { false }
    }

    fun onAppResumed(intent: Intent) {
        openNotificationsSettings =
            intent.hasCategory(NotificationCompat.INTENT_CATEGORY_NOTIFICATION_PREFERENCES)
        openAppSettings =
            isMinSdk(Build.VERSION_CODES.N) && intent.action == Intent.ACTION_APPLICATION_PREFERENCES

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

        loadAccounts()
    }

    @ExperimentalPermissionsApi
    fun onPermissionCheckStatus(status: PermissionStatus) {
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

    fun onPermissionsRequested() {
        isNeedToRequestNotifications.update { false }
    }

    fun onNotificationsDeniedDialogConfirmClicked() {
        isNeedToRequestNotifications.update { true }
    }

    fun onNotificationsDeniedDialogCancelClicked() {
        isNeedToShowNotificationsDeniedDialog.update { false }
        disableBackgroundLongPoll()
    }

    fun onNotificationsDeniedDialogDismissed() {
        isNeedToShowNotificationsDeniedDialog.update { false }
    }

    fun onNotificationsRationaleDialogDismissed() {
        isNeedToShowNotificationsRationaleDialog.update { false }
    }

    fun onNotificationsRationaleDialogCancelClicked() {
        isNeedToShowNotificationsRationaleDialog.update { false }
        disableBackgroundLongPoll()
    }

    fun onUserAuthenticated() {
        loadProfile()
    }

    private fun loadProfile() {
        loadUserByIdUseCase(userId = null)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = { error ->
                        currentUser.emit(null)
                    },
                    success = { response ->
                        val user = response ?: return@listenValue
                        currentUser.emit(user)
                    }
                )
            }
    }

    private fun listenLongPollState() {
        longPollController.stateToApply.listenValue(viewModelScope) { newState ->
            if (newState == LongPollState.Background) {
                isNeedToCheckNotificationsPermission.update { true }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentAccount = getCurrentAccountUseCase()?.mapToDto()

            logger.debug(
                this@MainViewModel::class,
                "loadAccounts(): currentAccount: %s"
                    .format(
                        currentAccount?.copy(
                            accessToken = if (currentAccount.accessToken.isNotEmpty()) "<redacted>"
                            else "null"
                        )
                    )
            )

            listenLongPollState()

            if (currentAccount != null) {
                UserConfig.apply {
                    this.userId = currentAccount.userId
                    this.accessToken = currentAccount.accessToken
                    this.fastToken = currentAccount.fastToken
                    this.trustedHash = currentAccount.trustedHash
                }

                longPollController.setStateToApply(
                    if (AppSettings.Experimental.longPollInBackground) {
                        LongPollState.Background
                    } else {
                        LongPollState.InApp
                    }
                )
            }

            if (currentAccount != null) {
                loadProfile()
            }

            startDestination.setValue {
                when {
                    openAppSettings -> Settings
                    openNotificationsSettings -> Settings
                    currentAccount == null -> AuthGraph
                    else -> Main
                }
            }
        }
    }

    private fun disableBackgroundLongPoll() {
        AppSettings.Experimental.longPollInBackground = false
        longPollController.setStateToApply(LongPollState.InApp)
    }
}
