package dev.meloda.fast.auth.login

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.auth.login.model.CaptchaArguments
import dev.meloda.fast.auth.login.model.LoginDialog
import dev.meloda.fast.auth.login.model.LoginScreenState
import dev.meloda.fast.auth.login.model.LoginUserBannedArguments
import dev.meloda.fast.auth.login.model.LoginValidationArguments
import dev.meloda.fast.auth.login.model.LoginValidationResult
import dev.meloda.fast.auth.login.validation.LoginValidator
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.extensions.updateValue
import dev.meloda.fast.common.model.LongPollState
import dev.meloda.fast.data.State
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.api.auth.AuthRepository
import dev.meloda.fast.data.db.AccountsRepository
import dev.meloda.fast.data.processState
import dev.meloda.fast.data.success
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.domain.OAuthUseCase
import dev.meloda.fast.model.database.AccountEntity
import dev.meloda.fast.network.OAuthErrorDomain
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val oAuthUseCase: OAuthUseCase,
    private val authRepository: AuthRepository,
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val accountsRepository: AccountsRepository,
    private val loginValidator: LoginValidator,
    private val longPollController: LongPollController,
    private val userSettings: UserSettings
) : ViewModel() {
    private val _screenState = MutableStateFlow(LoginScreenState.EMPTY)
    val screenState = _screenState.asStateFlow()

    private val _loginDialog = MutableStateFlow<LoginDialog?>(null)
    val loginDialog = _loginDialog.asStateFlow()

    private val _validationArguments = MutableStateFlow<LoginValidationArguments?>(null)
    val validationArguments = _validationArguments.asStateFlow()

    private val _captchaArguments = MutableStateFlow<CaptchaArguments?>(null)
    val captchaArguments = _captchaArguments.asStateFlow()

    private val _userBannedArguments = MutableStateFlow<LoginUserBannedArguments?>(null)
    val userBannedArguments = _userBannedArguments.asStateFlow()

    private val _isNeedToOpenMain = MutableStateFlow(false)
    val isNeedToOpenMain = _isNeedToOpenMain.asStateFlow()

    private val _isNeedToClearCaptchaCode = MutableStateFlow(false)
    val isNeedToClearCaptchaCode = _isNeedToClearCaptchaCode.asStateFlow()

    private val _isNeedToClearValidationCode = MutableStateFlow(false)
    val isNeedToClearValidationCode = _isNeedToClearValidationCode.asStateFlow()

    private val validationState: StateFlow<List<LoginValidationResult>> =
        screenState.map(loginValidator::validate)
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(LoginValidationResult.Empty))

    private val captchaSid = MutableStateFlow<String?>(null)
    private val captchaCode = MutableStateFlow<String?>(null)
    private val validationSid = MutableStateFlow<String?>(null)
    private val validationCode = MutableStateFlow<String?>(null)

    init {
        captchaCode.listenValue(viewModelScope) {
            if (it != null) {
                login()
            }
        }
        validationCode.listenValue(viewModelScope) {
            if (it != null) {
                login()
            }
        }
    }

    fun onDialogConfirmed(dialog: LoginDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is LoginDialog.Error -> Unit
        }
    }

    fun onDialogDismissed(dialog: LoginDialog) {
        when (dialog) {
            is LoginDialog.Error -> Unit
        }

        _loginDialog.setValue { null }
    }

    fun onBackPressed() {
        _screenState.setValue { old -> old.copy(showLogo = true) }
    }

    fun onPasswordVisibilityButtonClicked() {
        _screenState.setValue { old -> old.copy(passwordVisible = !old.passwordVisible) }
    }

    fun onLoginInputChanged(newLogin: String) {
        _screenState.setValue { old ->
            old.copy(
                login = newLogin.trim(),
                loginError = false
            )
        }
    }

    fun onPasswordInputChanged(newPassword: String) {
        _screenState.setValue { old ->
            old.copy(
                password = newPassword.trim(),
                passwordError = false
            )
        }
    }

    fun onSignInButtonClicked() {
        if (screenState.value.isLoading) return

        if (screenState.value.showLogo) {
            _screenState.setValue { old -> old.copy(showLogo = false) }
            return
        }

        login()
    }

    fun onLogoClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            userSettings.onEnableDynamicColorsChanged(
                !userSettings.enableDynamicColors.value
            )
        }
    }

    fun onNavigatedToMain() {
        _isNeedToOpenMain.update { false }
    }

    fun onNavigatedToUserBanned() {
        _userBannedArguments.update { null }
    }

    fun onNavigatedToCaptcha() {
        _captchaArguments.update { null }
    }

    fun onNavigatedToValidation() {
        _validationArguments.update { null }
    }

    fun onValidationCodeReceived(code: String?) {
        validationCode.update { code }
    }

    fun onValidationCodeCleared() {
        _isNeedToClearValidationCode.update { false }
    }

    fun onCaptchaCodeReceived(code: String?) {
        captchaCode.update { code }
    }

    fun onCaptchaCodeCleared() {
        _isNeedToClearCaptchaCode.update { false }
    }

    private fun login(forceSms: Boolean = false) {
        val currentState = screenState.value.copy()

        Log.d(
            "LoginViewModel",
            "auth: login: ${currentState.login}; " +
                    "password: ${currentState.password}; " +
                    "2fa code: ${validationCode.value}; " +
                    "captcha code: ${captchaCode.value}"
        )

        processValidation()
        if (!validationState.value.contains(LoginValidationResult.Valid)) return

        _screenState.updateValue { copy(isLoading = true) }

        val currentValidationSid = validationSid.value
        val currentValidationCode = validationCode.value?.takeIf { currentValidationSid != null }
        val currentCaptchaSid = captchaSid.value
        val currentCaptchaCode = captchaCode.value?.takeIf { currentCaptchaSid != null }

        oAuthUseCase.getSilentToken(
            login = currentState.login,
            password = currentState.password,
            forceSms = forceSms,
            validationCode = currentValidationCode,
            captchaSid = currentCaptchaSid,
            captchaKey = currentCaptchaCode
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    Log.d("LoginViewModelImpl", "login: error: $error")

                    _screenState.updateValue { copy(isLoading = false) }
                    captchaSid.setValue { null }

                    parseError(error)
                },
                success = { response ->
                    val exceptionHandler =
                        CoroutineExceptionHandler { _, _ ->
                            _screenState.updateValue { copy(isLoading = false) }
                            _loginDialog.setValue { LoginDialog.Error() }
                        }

                    viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
                        val (anonymToken) = authRepository.getAnonymToken(
                            VkConstants.MESSENGER_APP_ID.toString(),
                            VkConstants.MESSENGER_APP_SECRET
                        ).success()

                        val exchangeSilentTokenResponse = authRepository.exchangeSilentToken(
                            anonymToken = anonymToken,
                            silentToken = response.silentToken,
                            silentUuid = response.silentTokenUuid
                        ).success()


                        val getExchangeTokenResponse =
                            authRepository.getExchangeToken(exchangeSilentTokenResponse.accessToken)
                                .success()

                        val exchangeToken =
                            getExchangeTokenResponse.usersTokens.firstOrNull {
                                it.userId == exchangeSilentTokenResponse.userId
                            }

                        if (exchangeToken == null) {
                            _screenState.updateValue { copy(isLoading = false) }
                            _loginDialog.setValue { LoginDialog.Error() }
                            return@launch
                        }

                        val userId = exchangeSilentTokenResponse.userId
                        val accessToken = exchangeSilentTokenResponse.accessToken

                        // TODO: 30-Mar-25, Danil Nikolaev: get fast's app token

                        val currentAccount = AccountEntity(
                            userId = userId,
                            accessToken = accessToken,
                            fastToken = null,
                            trustedHash = response.trustedHash,
                            exchangeToken = exchangeToken.commonToken
                        ).also { account ->
                            UserConfig.currentUserId = account.userId
                            UserConfig.userId = account.userId
                            UserConfig.accessToken = account.accessToken
                            UserConfig.fastToken = account.fastToken
                            UserConfig.trustedHash = account.trustedHash
                            UserConfig.exchangeToken = account.exchangeToken
                        }

                        accountsRepository.storeAccounts(listOf(currentAccount))

                        startLongPoll()

                        captchaSid.update { null }
                        validationSid.update { null }

                        loadUserByIdUseCase(
                            userId = userId,
                            fields = VkConstants.USER_FIELDS,
                            nomCase = null
                        ).listenValue(viewModelScope) { state ->
                            state.processState(
                                any = {
                                    _screenState.updateValue { copy(isLoading = false) }
                                },
                                error = ::parseError,
                                success = { user ->
                                    if (user == null) {
                                        _loginDialog.update { LoginDialog.Error() }
                                    } else {
                                        _screenState.updateValue { copy(login = "", password = "") }
                                        _isNeedToOpenMain.update { true }
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    private fun parseError(stateError: State.Error) {
        when (stateError) {
            is State.Error.OAuthError -> {
                when (val error = stateError.error) {
                    is OAuthErrorDomain.ValidationRequiredError -> {
                        val arguments = LoginValidationArguments(
                            validationSid = error.validationSid,
                            redirectUri = error.redirectUri,
                            phoneMask = error.phoneMask,
                            validationType = error.validationType.value,
                            canResendSms = error.validationResend == "sms"
                        )
                        _validationArguments.update { arguments }
                        validationSid.update { error.validationSid }
                    }

                    is OAuthErrorDomain.CaptchaRequiredError -> {
                        val arguments = CaptchaArguments(
                            captchaSid = error.captchaSid,
                            captchaImageUrl = error.captchaImageUrl
                        )
                        _captchaArguments.update { arguments }
                        captchaSid.update { error.captchaSid }
                    }

                    OAuthErrorDomain.InvalidCredentialsError -> {
                        _loginDialog.setValue {
                            LoginDialog.Error(errorText = "Wrong login or password.")
                        }
                    }

                    is OAuthErrorDomain.UserBannedError -> {
                        val arguments = LoginUserBannedArguments(
                            name = error.memberName,
                            message = error.message,
                            restoreUrl = error.restoreUrl,
                            accessToken = error.accessToken
                        )
                        _userBannedArguments.update { arguments }
                    }

                    OAuthErrorDomain.WrongValidationCode -> {
                        _isNeedToClearValidationCode.update { true }
                        validationCode.update { null }
                        _loginDialog.setValue {
                            LoginDialog.Error(errorText = "Wrong validation code.")
                        }
                    }

                    OAuthErrorDomain.WrongValidationCodeFormat -> {
                        _isNeedToClearValidationCode.update { true }
                        validationCode.update { null }
                        _loginDialog.setValue {
                            LoginDialog.Error(errorText = "Wrong validation code format.")
                        }
                    }

                    OAuthErrorDomain.TooManyTriesError -> {
                        _loginDialog.setValue {
                            LoginDialog.Error(errorText = "Too many tries. Try in another hour or later.")
                        }
                    }

                    OAuthErrorDomain.UnknownError -> {
                        _loginDialog.setValue { LoginDialog.Error() }
                    }
                }
            }

            else -> Unit
        }
    }

    private fun processValidation() {
        validationState.value.forEach { result ->
            when (result) {
                LoginValidationResult.LoginEmpty -> {
                    _screenState.setValue { old -> old.copy(loginError = true) }
                }

                LoginValidationResult.PasswordEmpty -> {
                    _screenState.setValue { old -> old.copy(passwordError = true) }
                }

                LoginValidationResult.Empty -> Unit
                LoginValidationResult.Valid -> Unit
            }
        }
    }

    private fun startLongPoll() {
        longPollController.setStateToApply(
            if (AppSettings.Experimental.longPollInBackground) {
                LongPollState.Background
            } else {
                LongPollState.InApp
            }
        )
    }
}
