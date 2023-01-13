package com.meloda.fast.screens.login

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.auth.AuthDirectRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.CaptchaRequiredEvent
import com.meloda.fast.base.viewmodel.UnknownErrorEvent
import com.meloda.fast.base.viewmodel.ValidationRequiredEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.model.AppAccount
import com.meloda.fast.screens.login.model.LoginFormState
import com.meloda.fast.screens.login.model.LoginValidationResult
import com.meloda.fast.screens.login.validation.LoginValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ILoginViewModel {
    val events: Flow<VkEvent>

    val formState: StateFlow<LoginFormState>

    val isLoadingInProgress: Flow<Boolean>

    val isNeedToShowLoginError: Flow<Boolean>
    val isNeedToShowPasswordError: Flow<Boolean>
    val isNeedToShowCaptchaError: Flow<Boolean>
    val isNeedToShowValidationError: Flow<Boolean>

    val isNeedToShowCaptchaDialog: Flow<Boolean>
    val isNeedToShowValidationDialog: Flow<Boolean>
    val isNeedToShowValidationToast: Flow<Boolean>
    val isNeedToShowFastLoginDialog: Flow<Boolean>
    val isNeedToShowErrorDialog: Flow<Boolean>

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)
    fun onCaptchaCodeInputChanged(newCaptcha: String)
    fun onValidationCodeInputChanged(newTwoFa: String)


    fun onCaptchaEventReceived(event: CaptchaRequiredEvent)

    fun onValidationEventReceived(event: ValidationRequiredEvent)

    fun onSignInButtonClicked()
    fun onSignInButtonLongClicked()

    fun onCaptchaDialogOkButtonClicked()

    fun onValidationDialogOkButtonClicked()

    fun onFastLoginDialogOkButtonClicked()

    fun onCaptchaDialogDismissed()
    fun onValidationDialogDismissed()
    fun onValidationToastShown()
    fun onFastLoginDialogDismissed()
    fun onErrorDialogDismissed()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val router: Router,
    private val accounts: AccountsDao,
    private val loginValidator: LoginValidator,
) : BaseViewModel(), ILoginViewModel {

    override val events: Flow<VkEvent>
        get() = tasksEvent

    override val formState = MutableStateFlow(LoginFormState.EMPTY)

    private val validationState: StateFlow<List<LoginValidationResult>> = formState.map { state ->
        loginValidator.validate(state)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, listOf(LoginValidationResult.Empty))

    override val isLoadingInProgress = MutableStateFlow(false)
    override val isNeedToShowLoginError = MutableStateFlow(false)
    override val isNeedToShowPasswordError = MutableStateFlow(false)
    override val isNeedToShowCaptchaError = MutableStateFlow(false)
    override val isNeedToShowValidationError = MutableStateFlow(false)
    override val isNeedToShowErrorDialog = MutableStateFlow(false)
    override val isNeedToShowCaptchaDialog = MutableStateFlow(false)
    override val isNeedToShowValidationDialog = MutableStateFlow(false)
    override val isNeedToShowValidationToast = MutableStateFlow(false)
    override val isNeedToShowFastLoginDialog = MutableStateFlow(false)

    override fun onLoginInputChanged(newLogin: String) {
        val newState = formState.value.copy(login = newLogin)
        formState.update { newState }
        isNeedToShowLoginError.update { false }
    }

    override fun onPasswordInputChanged(newPassword: String) {
        val newState = formState.value.copy(password = newPassword)
        formState.update { newState }
        isNeedToShowPasswordError.update { false }
    }

    override fun onCaptchaCodeInputChanged(newCaptcha: String) {
        val newState = formState.value.copy(captchaCode = newCaptcha)
        formState.update { newState }
        processValidation()
    }

    override fun onValidationCodeInputChanged(newTwoFa: String) {
        val newState = formState.value.copy()
        formState.update { newState }
        processValidation()
    }

    override fun onCaptchaEventReceived(event: CaptchaRequiredEvent) {
        val newForm = formState.value.copy(
            captchaSid = event.sid,
            captchaImage = event.image
        )

        viewModelScope.launch { formState.emit(newForm) }
    }

    override fun onValidationEventReceived(event: ValidationRequiredEvent) {
        val newForm = formState.value.copy(
            validationSid = event.sid
        )

        viewModelScope.launch {
            formState.emit(newForm)
            isNeedToShowValidationToast.emit(true)

            sendValidationCode()
        }
    }

    override fun onSignInButtonClicked() {
        login()
    }

    override fun onSignInButtonLongClicked() {
        viewModelScope.launch { isNeedToShowFastLoginDialog.emit(true) }
    }

    override fun onCaptchaDialogOkButtonClicked() {
        login()
    }

    override fun onValidationDialogOkButtonClicked() {
        login()
    }

    override fun onFastLoginDialogOkButtonClicked() {
        login()
    }

    override fun onCaptchaDialogDismissed() {
        isNeedToShowCaptchaDialog.tryEmit(false)
    }

    override fun onValidationDialogDismissed() {
        isNeedToShowValidationDialog.tryEmit(false)
    }

    override fun onValidationToastShown() {
        isNeedToShowValidationToast.tryEmit(false)
    }

    override fun onFastLoginDialogDismissed() {
        isNeedToShowFastLoginDialog.tryEmit(false)
    }

    override fun onErrorDialogDismissed() {
        isNeedToShowErrorDialog.tryEmit(false)
    }

    private fun login(forceSms: Boolean = false) {
        processValidation()
        if (!validationState.value.contains(LoginValidationResult.Valid)) return

        val form = formState.value

        viewModelScope.launch(Dispatchers.IO) {
            makeJob(
                {
                    authRepository.auth(
                        AuthDirectRequest(
                            grantType = VKConstants.Auth.GrantType.PASSWORD,
                            clientId = VKConstants.VK_APP_ID,
                            clientSecret = VKConstants.VK_SECRET,
                            username = form.login,
                            password = form.password,
                            scope = VKConstants.Auth.SCOPE,
                            twoFaForceSms = forceSms,
                            twoFaCode = form.validationCode,
                            captchaSid = form.captchaSid,
                            captchaKey = form.captchaCode
                        )
                    )
                },
                onAnswer = {
                    if (it.userId == null || it.accessToken == null) {
                        sendEvent(UnknownErrorEvent)
                        return@makeJob
                    }

                    val currentAccount = AppAccount(
                        userId = it.userId,
                        accessToken = it.accessToken,
                        fastToken = null
                    ).also { account ->
                        UserConfig.currentUserId = account.userId
                        UserConfig.userId = account.userId
                        UserConfig.accessToken = account.accessToken
                        UserConfig.fastToken = account.fastToken
                    }

                    accounts.insert(listOf(currentAccount))

                    router.replaceScreen(Screens.Main())
                },
                onAnyResult = {
                    val newForm = formState.value.copy(
                        captchaSid = null,
                        captchaImage = null,
                        captchaCode = "",
                        validationSid = null,
                        validationCode = ""
                    )
                    formState.update { newForm }
                }
            )
        }
    }

    private fun sendValidationCode() {
        val validationSid = formState.value.validationSid ?: return

        viewModelScope.launch {
            makeJob(
                { authRepository.sendSms(validationSid) },
                onAnswer = {
                    isNeedToShowValidationDialog.emit(true)
                }
            )
        }
    }

    private fun processValidation() {
        val validationResults = validationState.value

        validationResults.forEach { result ->
            when (result) {
                LoginValidationResult.LoginEmpty -> isNeedToShowLoginError.tryEmit(true)
                LoginValidationResult.PasswordEmpty -> isNeedToShowPasswordError.tryEmit(true)
                LoginValidationResult.CaptchaEmpty -> isNeedToShowCaptchaError.tryEmit(true)
                LoginValidationResult.ValidationEmpty -> isNeedToShowValidationError.tryEmit(true)
                LoginValidationResult.Empty -> Unit
                LoginValidationResult.Valid -> Unit
            }
        }
    }
}
