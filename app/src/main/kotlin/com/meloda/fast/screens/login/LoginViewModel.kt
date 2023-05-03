package com.meloda.fast.screens.login

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.WrongTwoFaCodeError
import com.meloda.fast.api.network.WrongTwoFaCodeFormatError
import com.meloda.fast.api.network.auth.AuthDirectRequest
import com.meloda.fast.base.viewmodel.CaptchaRequiredEvent
import com.meloda.fast.base.viewmodel.DeprecatedBaseViewModel
import com.meloda.fast.base.viewmodel.UnknownErrorEvent
import com.meloda.fast.base.viewmodel.ValidationRequiredEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.ext.emitOnMainScope
import com.meloda.fast.ext.emitOnScope
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.updateValue
import com.meloda.fast.model.AppAccount
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.captcha.screen.CaptchaArguments
import com.meloda.fast.screens.captcha.screen.CaptchaResult
import com.meloda.fast.screens.captcha.screen.CaptchaScreen
import com.meloda.fast.screens.login.model.LoginScreenState
import com.meloda.fast.screens.login.model.LoginValidationResult
import com.meloda.fast.screens.login.validation.LoginValidator
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.twofa.model.TwoFaResult
import com.meloda.fast.screens.twofa.model.TwoFaValidationType
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LoginViewModel {
    val events: Flow<VkEvent>

    val isNeedToShowLogo: StateFlow<Boolean>

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
}

class LoginViewModelImpl constructor(
    private val authRepository: AuthRepository,
    private val router: Router,
    private val accounts: AccountsDao,
    private val loginValidator: LoginValidator,
    private val captchaScreen: CaptchaScreen,
    private val twoFaScreen: TwoFaScreen
) : DeprecatedBaseViewModel(), LoginViewModel {

    override val isNeedToShowLogo = MutableStateFlow(true)

    override val screenState = MutableStateFlow(LoginScreenState.EMPTY)

    private val validationState: StateFlow<List<LoginValidationResult>> =
        screenState.map(loginValidator::validate)
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(LoginValidationResult.Empty))

    private val captchaResult = captchaScreen.resultFlow
    private val twoFaResult = twoFaScreen.resultFlow

    override val events = MutableSharedFlow<VkEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val isNeedToShowErrorDialog = MutableStateFlow(false)
    override val isNeedToShowFastLoginDialog = MutableStateFlow(false)

    private var currentValidationEvent: ValidationRequiredEvent? = null

    init {
        tasksEvent.listenValue(::handleEvent)

        captchaResult.listenValue { result ->
            when (result) {
                is CaptchaResult.Success -> {
                    val sid = result.sid
                    val code = result.code
                    val newState = screenState.value.copy(
                        captchaSid = sid, captchaCode = code
                    )
                    screenState.updateValue(newState)

                    login()
                }

                else -> Unit
            }
        }

        twoFaResult.listenValue { result ->
            when (result) {
                is TwoFaResult.Success -> {
                    val sid = result.sid
                    val code = result.code
                    val newState = screenState.value.copy(
                        validationSid = sid, validationCode = code
                    )
                    screenState.updateValue(newState)

                    login()
                }

                else -> Unit
            }
        }
    }

    private fun handleEvent(event: VkEvent) {
        when (event) {
            is CaptchaRequiredEvent -> onCaptchaEventReceived(event)
            is ValidationRequiredEvent -> onValidationEventReceived(event)
            else -> events.emitOnScope(event)
        }
    }

    override fun onBackPressed() {
        if (isNeedToShowLogo.value) {
            router.exit()
        } else {
            isNeedToShowLogo.updateValue(true)
        }
    }

    override fun onPasswordVisibilityButtonClicked() {
        val newState = screenState.value.copy(
            passwordVisible = !screenState.value.passwordVisible
        )
        screenState.updateValue(newState)
    }

    override fun onLogoNextButtonClicked() {
        isNeedToShowLogo.emitOnMainScope(false)
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
            captchaSid = captchaSid,
            captchaImage = captchaImage
        )
        screenState.update { newState }

        showCaptchaScreen(
            CaptchaArguments(
                captchaSid = captchaSid,
                captchaImage = captchaImage
            )
        )
    }

    private fun showCaptchaScreen(args: CaptchaArguments) {
        captchaScreen.show(router, args)
    }

    private fun onValidationEventReceived(event: ValidationRequiredEvent) {
        currentValidationEvent = event

        val validationSid = event.sid
        val newForm = screenState.value.copy(
            validationSid = validationSid
        )
        screenState.update { newForm }

        showValidationScreen(
            TwoFaArguments(
                validationSid = event.sid,
                redirectUri = event.redirectUri,
                phoneMask = event.phoneMask,
                validationType = TwoFaValidationType.parse(event.validationType),
                canResendSms = event.canResendSms,
                wrongCodeError = event.codeError
            )
        )
    }

    private fun showValidationScreen(args: TwoFaArguments) {
        twoFaScreen.show(router, args)
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

    override fun onLogoLongClicked() {
        router.navigateTo(Screens.Settings())
    }

    private fun login(forceSms: Boolean = false) {
        currentValidationEvent?.let { event ->
            if (!screenState.value.validationSid.isNullOrBlank() && screenState.value.validationCode == null) {
                handleEvent(event)
                return
            }
        }

        val state = screenState.value.copy()

        val clearedState = screenState.value.copy(
            captchaSid = null,
            captchaImage = null,
            captchaCode = null,
            validationSid = null,
            validationCode = null
        )

        screenState.update { clearedState }

        processValidation()
        if (!validationState.value.contains(LoginValidationResult.Valid)) return

        viewModelScope.launch(Dispatchers.IO) {
            var newState = screenState.value.copy(
                isLoading = true
            )
            screenState.update { newState }

            sendRequest(
                onError = { error ->
                    when (error) {
                        is WrongTwoFaCodeError, WrongTwoFaCodeFormatError -> {
                            currentValidationEvent?.let { event ->
                                val codeError = UiText.Simple(
                                    if (error is WrongTwoFaCodeError) "Wrong code"
                                    else "Wrong code format"
                                )
                                handleEvent(event.copy(codeError = codeError))
                                true
                            } ?: false
                        }

                        else -> false
                    }
                },
                request = {
                    val requestModel = AuthDirectRequest(
                        grantType = VKConstants.Auth.GrantType.PASSWORD,
                        clientId = VKConstants.VK_APP_ID,
                        clientSecret = VKConstants.VK_SECRET,
                        username = state.login,
                        password = state.password,
                        scope = VKConstants.Auth.SCOPE,
                        twoFaForceSms = forceSms,
                        twoFaCode = state.validationCode,
                        captchaSid = state.captchaSid,
                        captchaKey = state.captchaCode
                    )

                    authRepository.auth(requestModel)
                }
            )?.let { response ->
                val userId = response.userId
                val accessToken = response.accessToken

                if (userId == null || accessToken == null) {
                    sendEvent(UnknownErrorEvent)
                    return@let
                }

                if (currentValidationEvent != null) {
                    currentValidationEvent = null
                }

                val currentAccount = AppAccount(
                    userId = userId,
                    accessToken = accessToken,
                    fastToken = null
                ).also { account ->
                    UserConfig.currentUserId = account.userId
                    UserConfig.userId = account.userId
                    UserConfig.accessToken = account.accessToken
                    UserConfig.fastToken = account.fastToken
                }

                accounts.insert(listOf(currentAccount))

                router.replaceScreen(Screens.Main())
            }

            newState = screenState.value.copy(
                isLoading = false
            )
            screenState.update { newState }
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
