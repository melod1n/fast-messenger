package com.meloda.fast.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.BuildConfig
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.CaptchaRequiredEvent
import com.meloda.fast.base.viewmodel.ValidationRequiredEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.database.account.AccountsDao
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.ext.emitOnMainScope
import com.meloda.fast.ext.setValue
import com.meloda.fast.ext.updateValue
import com.meloda.fast.model.AppAccount
import com.meloda.fast.screens.captcha.model.CaptchaArguments
import com.meloda.fast.screens.login.model.LoginScreenState
import com.meloda.fast.screens.login.model.LoginValidationResult
import com.meloda.fast.screens.login.validation.LoginValidator
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LoginViewModel {
    val screenState: StateFlow<LoginScreenState>

    val isNeedToShowFastLoginDialog: Flow<Boolean>
    val isNeedToShowErrorDialog: Flow<Boolean>

    fun onBackPressed()

    fun onPasswordVisibilityButtonClicked()

    fun onLogoNextButtonClicked()

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)

    fun onSignInButtonClicked()
    fun onSignInButtonLongClicked()

    fun onFastLoginDialogOkButtonClicked()

    fun onFastLoginDialogDismissed()
    fun onErrorDialogDismissed()
    fun onLogoLongClicked()

    fun onNavigatedToCaptcha()
    fun onNavigatedToTwoFa()
    fun onNavigatedToConversations()

    fun onTwoFaCodeReceived(code: String)
    fun onCaptchaCodeReceived(code: String)
    fun onRestarted()
}

class LoginViewModelImpl(
    private val authRepository: AuthRepository,
    private val accounts: AccountsDao,
    private val loginValidator: LoginValidator,
) : ViewModel(), LoginViewModel {

    override val screenState = MutableStateFlow(LoginScreenState.EMPTY)

    private val validationState: StateFlow<List<LoginValidationResult>> =
        screenState.map(loginValidator::validate)
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(LoginValidationResult.Empty))

    override val isNeedToShowErrorDialog = MutableStateFlow(false)
    override val isNeedToShowFastLoginDialog = MutableStateFlow(false)

    private var currentValidationEvent: ValidationRequiredEvent? = null

    init {
//        events.listenValue(::handleEvent)
    }

    private fun handleEvent(event: VkEvent) {
        when (event) {
            is CaptchaRequiredEvent -> onCaptchaEventReceived(event)
            is ValidationRequiredEvent -> onValidationEventReceived(event)
            else -> Unit
        }
    }

    override fun onBackPressed() {
        screenState.setValue { old -> old.copy(isNeedToShowLogo = true) }
    }

    override fun onPasswordVisibilityButtonClicked() {
        val newState = screenState.value.copy(
            passwordVisible = !screenState.value.passwordVisible
        )
        screenState.updateValue(newState)
    }

    override fun onLogoNextButtonClicked() {
        screenState.setValue { old -> old.copy(isNeedToShowLogo = false) }
    }

    override fun onLoginInputChanged(newLogin: String) {
        val newState = screenState.value.copy(
            login = newLogin.trim(),
            loginError = false
        )
        screenState.updateValue(newState)
    }

    override fun onPasswordInputChanged(newPassword: String) {
        val newState = screenState.value.copy(
            password = newPassword.trim(),
            passwordError = false
        )
        screenState.updateValue(newState)
    }

    private fun onCaptchaEventReceived(event: CaptchaRequiredEvent) {
        val captchaSid = event.sid
        val captchaImage = event.image

        val newState = screenState.value.copy(
            captchaArguments = CaptchaArguments(
                captchaSid = captchaSid,
                captchaImage = captchaImage
            )
        )
        screenState.update { newState }

        showCaptchaScreen()
    }

    private fun showCaptchaScreen() {
        screenState.updateValue(
            screenState.value.copy(isNeedToOpenCaptcha = true)
        )
    }

    private fun onValidationEventReceived(event: ValidationRequiredEvent) {
        currentValidationEvent = event

        val newForm = screenState.value.copy(
            twoFaArguments = TwoFaArguments(
                validationSid = event.sid,
                redirectUri = event.redirectUri,
                phoneMask = event.phoneMask,
                validationType = event.validationType,
                canResendSms = event.canResendSms,
                wrongCodeError = event.codeError,
            ),
            isNeedToOpenTwoFa = true
        )
        screenState.update { newForm }
    }

    override fun onSignInButtonClicked() {
        login()
    }

    override fun onSignInButtonLongClicked() {
        isNeedToShowFastLoginDialog.emitOnMainScope(true)
    }

    override fun onFastLoginDialogOkButtonClicked() {
        login()
    }

    override fun onFastLoginDialogDismissed() {
        isNeedToShowFastLoginDialog.emitOnMainScope(false)
    }

    override fun onErrorDialogDismissed() {
        isNeedToShowErrorDialog.emitOnMainScope(false)
    }

    // TODO: 08/11/2023, Danil Nikolaev: add confirmation
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
            accounts.insert(listOf(currentAccount))

            screenState.setValue { old -> old.copy(isNeedToRestart = true) }
        }
    }

    override fun onNavigatedToCaptcha() {
        screenState.updateValue(screenState.value.copy(isNeedToOpenCaptcha = false))
    }

    override fun onNavigatedToTwoFa() {
        screenState.updateValue(screenState.value.copy(isNeedToOpenTwoFa = false))
    }

    override fun onNavigatedToConversations() {
        screenState.updateValue(screenState.value.copy(isNeedToOpenConversations = false))
    }

    override fun onTwoFaCodeReceived(code: String) {
        screenState.updateValue(
            screenState.value.copy(validationCode = code)
        )

        login()
    }

    override fun onCaptchaCodeReceived(code: String) {
        screenState.updateValue(
            screenState.value.copy(captchaCode = code)
        )

        login()
    }

    override fun onRestarted() {
        screenState.setValue { old -> old.copy(isNeedToRestart = false) }
    }

    private fun login(forceSms: Boolean = false) {
        currentValidationEvent?.let { event ->
            if (!screenState.value.validationSid.isNullOrBlank() && screenState.value.validationCode == null) {
                handleEvent(event)
                return
            }
        }

        val state = screenState.value.copy()

        Log.d(
            "LoginViewModel",
            "auth: login: ${state.login}; password: ${state.password}; code: ${state.validationCode}"
        )

        val clearedState = screenState.value.copy(
            captchaArguments = null,
            captchaCode = null,
            validationSid = null,
            validationCode = null,
            twoFaArguments = null
        )

        screenState.update { clearedState }

        processValidation()
        if (!validationState.value.contains(LoginValidationResult.Valid)) return

        viewModelScope.launch(Dispatchers.IO) {
            var newState = screenState.value.copy(
                isLoading = true
            )
            screenState.update { newState }

//            sendRequest(
//                onError = ::parseError,
//                request = {
//                    val requestModel = AuthDirectRequest(
//                        grantType = VKConstants.Auth.GrantType.PASSWORD,
//                        clientId = VKConstants.VK_APP_ID,
//                        clientSecret = VKConstants.VK_SECRET,
//                        username = state.login,
//                        password = state.password,
//                        scope = VKConstants.Auth.SCOPE,
//                        twoFaForceSms = forceSms,
//                        twoFaCode = state.validationCode,
//                        captchaSid = state.captchaArguments?.captchaSid,
//                        captchaKey = state.captchaCode,
//                    )
//
//                    authRepository.auth(requestModel)
//                }
//            )?.let { response ->
//                val userId = response.userId
//                val accessToken = response.accessToken
//
//                // TODO: 02/12/2023, Danil Nikolaev: implement loading user info
//
//                if (userId == null || accessToken == null) {
////                    sendEvent(UnknownErrorEvent)
//                    return@let
//                }
//
//                if (currentValidationEvent != null) {
//                    currentValidationEvent = null
//                }
//
//                val currentAccount = AppAccount(
//                    userId = userId,
//                    accessToken = accessToken,
//                    fastToken = null,
//                    trustedHash = response.twoFaHash
//                ).also { account ->
//                    UserConfig.currentUserId = account.userId
//                    UserConfig.userId = account.userId
//                    UserConfig.accessToken = account.accessToken
//                    UserConfig.fastToken = account.fastToken
//                    UserConfig.trustedHash = account.trustedHash
//                }
//
//                accounts.insert(listOf(currentAccount))
//
//                screenState.updateValue(screenState.value.copy(isNeedToOpenConversations = true))
//            }

            newState = screenState.value.copy(
                isLoading = false
            )
            screenState.update { newState }
        }
    }

    private fun parseError(error: Throwable): Boolean {
        return when (error) {
//            is WrongTwoFaCodeError, WrongTwoFaCodeFormatError -> {
//                currentValidationEvent?.let { event ->
//                    val codeError = UiText.Simple(
//                        if (error is WrongTwoFaCodeError) "Wrong code"
//                        else "Wrong code format"
//                    )
//                    handleEvent(event.copy(codeError = codeError))
//                    true
//                } ?: false
//            }

//            is ValidationRequiredError -> {
//                handleEvent(
//                    ValidationRequiredEvent(
//                        sid = error.validationSid,
//                        redirectUri = error.redirectUri,
//                        phoneMask = error.phoneMask,
//                        validationType = error.validationType,
//                        canResendSms = error.validationResend == "sms",
//                        codeError = null
//                    )
//                )
//                true
//            }
//
//            is CaptchaRequiredError -> {
//                handleEvent(
//                    CaptchaRequiredEvent(
//                        sid = error.captchaSid,
//                        image = error.captchaImg
//                    )
//                )
//                true
//            }

            else -> false
        }
    }

    private fun processValidation() {
        validationState.value.forEach { result ->
            when (result) {
                LoginValidationResult.LoginEmpty -> {
                    screenState.updateValue(screenState.value.copy(loginError = true))
                }

                LoginValidationResult.PasswordEmpty -> {
                    screenState.updateValue(screenState.value.copy(passwordError = true))
                }

                LoginValidationResult.Empty -> Unit
                LoginValidationResult.Valid -> Unit
            }
        }
    }
}
