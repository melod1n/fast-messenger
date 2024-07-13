package com.meloda.fast.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.auth.login.BuildConfig
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.extensions.updateValue
import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.users.UsersUseCase
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.model.database.AccountEntity
import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.fast.auth.login.model.CaptchaArguments
import com.meloda.fast.auth.login.model.LoginError
import com.meloda.fast.auth.login.model.LoginScreenState
import com.meloda.fast.auth.login.model.LoginValidationArguments
import com.meloda.fast.auth.login.model.LoginUserBannedArguments
import com.meloda.fast.auth.login.model.LoginValidationResult
import com.meloda.fast.auth.login.validation.LoginValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface LoginViewModel {
    val screenState: StateFlow<LoginScreenState>
    val loginError: StateFlow<LoginError?>

    val validationCode: StateFlow<String?>
    val validationArguments: StateFlow<LoginValidationArguments?>
    val captchaCode: StateFlow<String?>
    val captchaArguments: StateFlow<CaptchaArguments?>
    val userBannedArguments: StateFlow<LoginUserBannedArguments?>
    val isNeedToOpenMain: StateFlow<Boolean>

    fun onPasswordVisibilityButtonClicked()

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)

    fun onSignInButtonClicked()

    fun onErrorDialogDismissed()

    fun onNavigatedToMain()
    fun onNavigatedToUserBanned()
    fun onNavigatedToCaptcha()
    fun onNavigatedToValidation()

    fun onValidationCodeReceived(code: String)
    fun onCaptchaCodeReceived(code: String)

    fun onLogoLongClicked()
}

class LoginViewModelImpl(
    private val oAuthUseCase: OAuthUseCase,
    private val usersUseCase: UsersUseCase,
    private val accountsRepository: AccountsRepository,
    private val loginValidator: LoginValidator
) : ViewModel(), LoginViewModel {

    override val screenState = MutableStateFlow(LoginScreenState.EMPTY)
    override val loginError = MutableStateFlow<LoginError?>(null)

    override val validationCode = MutableStateFlow<String?>(null)
    override val validationArguments = MutableStateFlow<LoginValidationArguments?>(null)
    override val captchaCode = MutableStateFlow<String?>(null)
    override val captchaArguments = MutableStateFlow<CaptchaArguments?>(null)
    override val userBannedArguments = MutableStateFlow<LoginUserBannedArguments?>(null)
    override val isNeedToOpenMain = MutableStateFlow(false)

    private val validationState: StateFlow<List<LoginValidationResult>> =
        screenState.map(loginValidator::validate)
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(LoginValidationResult.Empty))

    override fun onPasswordVisibilityButtonClicked() {
        screenState.setValue { old -> old.copy(passwordVisible = !old.passwordVisible) }
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

    override fun onSignInButtonClicked() {
        if (screenState.value.isLoading) return
        login()
    }

    override fun onErrorDialogDismissed() {
        loginError.update { null }
    }

    override fun onNavigatedToMain() {
        isNeedToOpenMain.update { false }
    }

    override fun onNavigatedToUserBanned() {
        userBannedArguments.update { null }
    }

    override fun onNavigatedToCaptcha() {
        captchaArguments.update { null }
    }

    override fun onNavigatedToValidation() {
        validationArguments.update { null }
    }

    override fun onValidationCodeReceived(code: String) {
        validationCode.update { code }

        login()
    }

    override fun onCaptchaCodeReceived(code: String) {
        captchaCode.update { code }

        login()
    }

    override fun onLogoLongClicked() {
        val currentAccount = AccountEntity(
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

        usersUseCase.get(
            userIds = null,
            fields = VkConstants.USER_FIELDS,
            nomCase = null
        ).listenValue { state ->
            state.processState(
                error = { error ->

                },
                success = { response ->
                    viewModelScope.launch(Dispatchers.IO) {
                        accountsRepository.storeAccounts(listOf(currentAccount))

                        delay(350)

                        isNeedToOpenMain.update { true }
                    }
                }
            )

            screenState.setValue { old -> old.copy(isLoading = state.isLoading()) }
        }
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

        oAuthUseCase.auth(
            login = currentState.login,
            password = currentState.password,
            forceSms = forceSms,
            validationCode = validationCode.value,
            captchaSid = captchaArguments.value?.captchaSid,
            captchaKey = captchaCode.value
        ).listenValue { state ->
            state.processState(
                error = { error ->
                    Log.d("LoginViewModelImpl", "login: error: $error")

                    validationCode.update { null }
                    captchaCode.update { null }

                    parseError(error)
                },
                success = { response ->
                    val userId = response.userId
                    val accessToken = response.accessToken

                    if (userId == null || accessToken == null) {
                        // TODO: 11/04/2024, Danil Nikolaev: send unknown error event
                        return@processState
                    }

                    usersUseCase.get(
                        userIds = listOf(userId),
                        fields = VkConstants.USER_FIELDS,
                        nomCase = null
                    )

                    val currentAccount = AccountEntity(
                        userId = userId,
                        accessToken = accessToken,
                        fastToken = null,
                        trustedHash = response.validationHash
                    ).also { account ->
                        UserConfig.currentUserId = account.userId
                        UserConfig.userId = account.userId
                        UserConfig.accessToken = account.accessToken
                        UserConfig.fastToken = account.fastToken
                        UserConfig.trustedHash = account.trustedHash
                    }

                    accountsRepository.storeAccounts(listOf(currentAccount))

                    captchaArguments.update { null }
                    captchaCode.update { null }

                    validationArguments.update { null }
                    validationCode.update { null }

                    screenState.setValue { old ->
                        old.copy(
                            login = "",
                            password = "",
                        )
                    }

                    isNeedToOpenMain.update { true }
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun parseError(stateError: State.Error): Boolean {
        return when (stateError) {
            is State.Error.OAuthError -> {
                when (val error = stateError.error) {
                    is OAuthErrorDomain.ValidationRequiredError -> {
                        val arguments = LoginValidationArguments(
                            validationSid = error.validationSid,
                            redirectUri = error.redirectUri,
                            phoneMask = error.phoneMask,
                            validationType = error.validationType.value,
                            canResendSms = error.validationResend == "sms",
                            wrongCodeError = null
                        )
                        validationArguments.update { arguments }
                    }

                    is OAuthErrorDomain.CaptchaRequiredError -> {
                        val arguments = CaptchaArguments(
                            captchaSid = error.captchaSid,
                            captchaImageUrl = error.captchaImageUrl
                        )
                        captchaArguments.update { arguments }
                    }

                    OAuthErrorDomain.InvalidCredentialsError -> {
                        loginError.update { LoginError.WrongCredentials }
                    }

                    is OAuthErrorDomain.UserBannedError -> {
                        val arguments = LoginUserBannedArguments(
                            name = error.memberName,
                            message = error.message,
                            restoreUrl = error.restoreUrl,
                            accessToken = error.accessToken
                        )
                        userBannedArguments.update { arguments }
                    }

                    OAuthErrorDomain.WrongValidationCode -> {
                        loginError.update { LoginError.WrongValidationCode }
                    }

                    OAuthErrorDomain.WrongValidationCodeFormat -> {
                        loginError.update { LoginError.WrongValidationCodeFormat }
                    }

                    OAuthErrorDomain.TooManyTriesError -> {
                        loginError.update { LoginError.TooManyTries }
                    }

                    OAuthErrorDomain.UnknownError -> {
                        loginError.update { LoginError.Unknown }
                    }
                }

                true
            }

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
