package dev.meloda.fast

import android.content.Intent
import android.os.Build
import android.util.Log
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
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.navigation.Main
import dev.meloda.fast.settings.navigation.Settings
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

    val profileImageUrl: StateFlow<String?>

    fun onError(error: BaseError)

    fun onNavigatedToAuth()

    fun onAppResumed(intent: Intent)

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
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val userSettings: UserSettings,
    private val longPollController: LongPollController
) : MainViewModel, ViewModel() {

    override val startDestination = MutableStateFlow<Any?>(null)
    override val isNeedToReplaceWithAuth = MutableStateFlow(false)

    override val isNeedToShowNotificationsDeniedDialog = MutableStateFlow(false)
    override val isNeedToShowNotificationsRationaleDialog = MutableStateFlow(false)
    override val isNeedToCheckNotificationsPermission = MutableStateFlow(false)
    override val isNeedToRequestNotifications = MutableStateFlow(false)

    override val profileImageUrl = MutableStateFlow<String?>(null)

    private var openNotificationsSettings = false
    private var openAppSettings = false

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

    override fun onAppResumed(intent: Intent) {
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

    private fun loadProfile() {
        loadUserByIdUseCase(userId = null)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = { error ->
                        profileImageUrl.emit(null)
                    },
                    success = { response ->
                        val user = response ?: return@listenValue

                        profileImageUrl.emit(user.photo100)
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
        AppSettings.Debug.longPollInBackground = false
        longPollController.setStateToApply(LongPollState.InApp)
    }
}
