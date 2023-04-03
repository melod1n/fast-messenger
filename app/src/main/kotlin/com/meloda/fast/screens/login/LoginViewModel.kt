package com.meloda.fast.screens.login

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.auth.AuthDirectRequest
import com.meloda.fast.base.viewmodel.*
import com.meloda.fast.common.Screens
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.ext.listenValue
import com.meloda.fast.model.AppAccount
import com.meloda.fast.screens.captcha.screen.CaptchaArguments
import com.meloda.fast.screens.captcha.screen.CaptchaResult
import com.meloda.fast.screens.captcha.screen.CaptchaScreen
import com.meloda.fast.screens.login.model.LoginScreenState
import com.meloda.fast.screens.login.model.LoginValidationResult
import com.meloda.fast.screens.login.validation.LoginValidator
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.twofa.screen.TwoFaResult
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface LoginViewModel {
    val events: Flow<VkEvent>

    val isNeedToShowLogo: StateFlow<Boolean>

    val screenState: StateFlow<LoginScreenState>

    val isLoadingInProgress: StateFlow<Boolean>

    val isNeedToShowLoginError: StateFlow<Boolean>
    val isNeedToShowPasswordError: StateFlow<Boolean>

    val isPasswordVisible: StateFlow<Boolean>

    val isNeedToShowFastLoginDialog: Flow<Boolean>
    val isNeedToShowErrorDialog: Flow<Boolean>

    fun onPasswordVisibilityButtonClicked()

    fun onLogoNextButtonClicked()

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)


    fun onCaptchaEventReceived(event: CaptchaRequiredEvent)

    fun onValidationEventReceived(event: ValidationRequiredEvent)

    fun onSignInButtonClicked()
    fun onSignInButtonLongClicked()

    fun onFastLoginDialogOkButtonClicked()

    fun onFastLoginDialogDismissed()
    fun onErrorDialogDismissed()
}

class LoginViewModelImpl(
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

    override val events = MutableStateFlow<VkEvent>(VkNoneEvent)
    override val isLoadingInProgress = MutableStateFlow(false)
    override val isNeedToShowLoginError = MutableStateFlow(false)
    override val isNeedToShowPasswordError = MutableStateFlow(false)
    override val isPasswordVisible = MutableStateFlow(false)
    override val isNeedToShowErrorDialog = MutableStateFlow(false)
    override val isNeedToShowFastLoginDialog = MutableStateFlow(false)

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
                    screenState.update { newState }

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
                    screenState.update { newState }

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
            else -> events.update { event }
        }
    }

    override fun onPasswordVisibilityButtonClicked() {
        val newVisibility = !isPasswordVisible.value
        isPasswordVisible.tryEmit(newVisibility)
    }

    override fun onLogoNextButtonClicked() {
        isNeedToShowLogo.tryEmit(false)
    }

    override fun onLoginInputChanged(newLogin: String) {
        val newState = screenState.value.copy(login = newLogin)
        screenState.update { newState }
        isNeedToShowLoginError.tryEmit(false)
    }

    override fun onPasswordInputChanged(newPassword: String) {
        val newState = screenState.value.copy(password = newPassword)
        screenState.update { newState }
        isNeedToShowPasswordError.tryEmit(false)
    }

    override fun onCaptchaEventReceived(event: CaptchaRequiredEvent) {
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

    override fun onValidationEventReceived(event: ValidationRequiredEvent) {
        val validationSid = event.sid
        val newForm = screenState.value.copy(
            validationSid = validationSid
        )
        screenState.update { newForm }

        showValidationScreen(TwoFaArguments(validationSid))

        sendValidationCode()
    }

    private fun showValidationScreen(args: TwoFaArguments) {
        twoFaScreen.show(router, args)
    }

    override fun onSignInButtonClicked() {
        login()
    }

    override fun onSignInButtonLongClicked() {
        viewModelScope.launch { isNeedToShowFastLoginDialog.emit(true) }
    }

    override fun onFastLoginDialogOkButtonClicked() {
        login()
    }

    override fun onFastLoginDialogDismissed() {
        isNeedToShowFastLoginDialog.tryEmit(false)
    }

    override fun onErrorDialogDismissed() {
        isNeedToShowErrorDialog.tryEmit(false)
    }

    private fun login(forceSms: Boolean = true) {
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

        isLoadingInProgress.update { true }

        viewModelScope.launch(Dispatchers.IO) {
            makeJob(
                {
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
                onAnyResult = { isLoadingInProgress.update { false } }
            )
        }
    }

    private fun sendValidationCode() {
        val validationSid = screenState.value.validationSid ?: return

        viewModelScope.launch {
            // TODO: 03.04.2023, Danil Nikolaev: handle response and error
            val response = sendRequest(
                request = { authRepository.sendSms(validationSid) },
                onError = { error -> false }
            )
        }
    }

    private fun processValidation() {
        validationState.value.forEach { result ->
            when (result) {
                LoginValidationResult.LoginEmpty -> isNeedToShowLoginError.tryEmit(true)
                LoginValidationResult.PasswordEmpty -> isNeedToShowPasswordError.tryEmit(true)
                LoginValidationResult.Empty -> Unit
                LoginValidationResult.Valid -> Unit
            }
        }
    }
}
