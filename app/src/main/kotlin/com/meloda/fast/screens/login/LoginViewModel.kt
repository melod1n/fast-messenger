package com.meloda.fast.screens.login

import android.util.Log
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
import com.meloda.fast.screens.twofa.screen.TwoFaResult
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface LoginViewModel {
    val events: Flow<VkEvent>

    val screenState: StateFlow<LoginScreenState>

    val isLoadingInProgress: Flow<Boolean>

    val isNeedToShowLoginError: Flow<Boolean>
    val isNeedToShowPasswordError: Flow<Boolean>
    val isNeedToShowCaptchaError: Flow<Boolean>
    val isNeedToShowValidationError: Flow<Boolean>

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
    override val isNeedToShowCaptchaError = MutableStateFlow(false)
    override val isNeedToShowValidationError = MutableStateFlow(false)
    override val isNeedToShowErrorDialog = MutableStateFlow(false)
    override val isNeedToShowFastLoginDialog = MutableStateFlow(false)

    init {
        tasksEvent.listenValue(::handleEvent)

        captchaResult.listenValue { result ->
            when (result) {
                is CaptchaResult.Success -> {
                    val code = result.code
                    val newState = screenState.value.copy(captchaCode = code)
                    screenState.update { newState }

                    Log.d("LoginViewModelImpl", "captchaCode: $code")
                }
                else -> Unit
            }
        }

        twoFaResult.listenValue { result ->
            when (result) {
                is TwoFaResult.Success -> {
                    val code = result.code
                    val newState = screenState.value.copy(validationCode = code)
                    screenState.update { newState }

                    Log.d("LoginViewModelImpl", "twoFaCode: $code")
                }
                else -> Unit
            }
        }

//        showCaptchaScreen(CaptchaArguments("https://api.vk.com/captcha.php?sid=346849433736"))
        showValidationScreen()
    }

    private fun handleEvent(event: VkEvent) {
        when (event) {
            is CaptchaRequiredEvent -> onCaptchaEventReceived(event)
            is ValidationRequiredEvent -> onValidationEventReceived(event)
            else -> events.update { event }
        }
    }

    override fun onLoginInputChanged(newLogin: String) {
        val newState = screenState.value.copy(login = newLogin)
        screenState.update { newState }
        isNeedToShowLoginError.update { false }
    }

    override fun onPasswordInputChanged(newPassword: String) {
        val newState = screenState.value.copy(password = newPassword)
        screenState.update { newState }
        isNeedToShowPasswordError.update { false }
    }

    override fun onCaptchaCodeInputChanged(newCaptcha: String) {
        val newState = screenState.value.copy(captchaCode = newCaptcha)
        screenState.update { newState }
        processValidation()
    }

    override fun onValidationCodeInputChanged(newTwoFa: String) {
        val newState = screenState.value.copy(validationCode = newTwoFa)
        screenState.update { newState }
        processValidation()
    }

    override fun onCaptchaEventReceived(event: CaptchaRequiredEvent) {
        val newForm = screenState.value.copy(
            captchaSid = event.sid,
            captchaImage = event.image
        )

        screenState.update { newForm }

        screenState.value.captchaImage?.let { image ->
            showCaptchaScreen(CaptchaArguments(image))
        }
    }

    private fun showCaptchaScreen(args: CaptchaArguments) {
        captchaScreen.show(router, args)
    }

    override fun onValidationEventReceived(event: ValidationRequiredEvent) {
        val newForm = screenState.value.copy(
            validationSid = event.sid
        )

        screenState.update { newForm }

        showValidationScreen()

        sendValidationCode()
    }

    private fun showValidationScreen() {
        twoFaScreen.show(router, Unit)
//        router.navigateTo(FragmentScreen { TwoFaFragment.newInstance() })
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

                    val newForm = screenState.value.copy(
                        captchaSid = null,
                        captchaImage = null,
                        captchaCode = null,
                        validationSid = null,
                        validationCode = null
                    )
                    screenState.update { newForm }
                }
            )
        }
    }

    private fun sendValidationCode() {
        val validationSid = screenState.value.validationSid ?: return

        viewModelScope.launch {
            makeJob(
                { authRepository.sendSms(validationSid) },
                onAnswer = {
                    // TODO: 29.03.2023, Danil Nikolaev: handle response
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
