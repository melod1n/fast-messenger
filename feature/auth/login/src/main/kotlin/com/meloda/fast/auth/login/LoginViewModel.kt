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
import com.meloda.fast.auth.login.model.LoginScreenState
import com.meloda.fast.auth.login.model.LoginTwoFaArguments
import com.meloda.fast.auth.login.model.LoginValidationResult
import com.meloda.fast.auth.login.validation.LoginValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface LoginViewModel {
    val screenState: StateFlow<LoginScreenState>

    fun onPasswordVisibilityButtonClicked()

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)

    fun onSignInButtonClicked()

    fun onErrorDialogDismissed()

    fun onNavigatedToMain()
    fun onNavigatedToUserBanned()
    fun onNavigatedToCaptcha()
    fun onNavigatedToTwoFa()

    fun onTwoFaCodeReceived(code: String)
    fun onCaptchaCodeReceived(code: String)

    fun onLogoLongClicked()
}

class LoginViewModelImpl(
    private val oAuthUseCase: OAuthUseCase,
    private val usersUseCase: UsersUseCase,
    private val accountsRepository: AccountsRepository,
    private val loginValidator: LoginValidator,
) : ViewModel(), LoginViewModel {

    override val screenState = MutableStateFlow(LoginScreenState.EMPTY)

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
        login()
    }

    override fun onErrorDialogDismissed() {
        screenState.setValue { old -> old.copy(error = null) }
    }

    override fun onNavigatedToMain() {
        screenState.setValue { old -> old.copy(isNeedToNavigateToMain = false) }
    }

    override fun onNavigatedToUserBanned() {
        screenState.setValue { old -> old.copy(userBannedArguments = null) }
    }

    override fun onNavigatedToCaptcha() {
        screenState.setValue { old -> old.copy(captchaArguments = null) }
    }

    override fun onNavigatedToTwoFa() {
        screenState.setValue { old -> old.copy(twoFaArguments = null) }
    }

    override fun onTwoFaCodeReceived(code: String) {
        screenState.setValue { old -> old.copy(validationCode = code) }

        login()
    }

    override fun onCaptchaCodeReceived(code: String) {
        screenState.setValue { old -> old.copy(captchaCode = code) }

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
                        screenState.setValue { old -> old.copy(isNeedToNavigateToMain = true) }
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
            "auth: login: ${currentState.login}; password: ${currentState.password}; code: ${currentState.validationCode}"
        )

        processValidation()
        if (!validationState.value.contains(LoginValidationResult.Valid)) return

        oAuthUseCase.auth(
            login = currentState.login,
            password = currentState.password,
            forceSms = forceSms,
            twoFaCode = currentState.validationCode,
            captchaSid = currentState.captchaArguments?.captchaSid,
            captchaKey = currentState.captchaCode
        ).listenValue { state ->
            state.processState(
                error = { error ->
                    Log.d("LoginViewModelImpl", "login: error: $error")

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
                        trustedHash = response.twoFaHash
                    ).also { account ->
                        UserConfig.currentUserId = account.userId
                        UserConfig.userId = account.userId
                        UserConfig.accessToken = account.accessToken
                        UserConfig.fastToken = account.fastToken
                        UserConfig.trustedHash = account.trustedHash
                    }

                    accountsRepository.storeAccounts(listOf(currentAccount))

                    screenState.setValue { old ->
                        old.copy(
                            captchaArguments = null,
                            captchaCode = null,
                            validationSid = null,
                            validationCode = null,
                            twoFaArguments = null,

                            login = "",
                            password = "",

                            isNeedToNavigateToMain = true
                        )
                    }
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
                        val twoFaArguments = LoginTwoFaArguments(
                            validationSid = error.validationSid,
                            redirectUri = error.redirectUri,
                            phoneMask = error.phoneMask,
                            validationType = error.validationType.value,
                            canResendSms = error.validationResend == "sms",
                            wrongCodeError = null
                        )
                        screenState.setValue { old -> old.copy(twoFaArguments = twoFaArguments) }
                        true
                    }

                    is OAuthErrorDomain.CaptchaRequiredError -> {
                        val captchaArguments = CaptchaArguments(
                            captchaSid = error.captchaSid,
                            captchaImage = error.captchaImageUrl
                        )
                        screenState.setValue { old -> old.copy(captchaArguments = captchaArguments) }
                        true
                    }

                    OAuthErrorDomain.InvalidCredentialsError -> TODO()
                    is OAuthErrorDomain.UserBannedError -> TODO()
                    OAuthErrorDomain.WrongTwoFaCode -> TODO()
                    OAuthErrorDomain.WrongTwoFaCodeFormat -> TODO()
                    OAuthErrorDomain.UnknownError -> TODO()
                }
            }

            else -> false
        }


//        return when (val error =
//            (stateError as? State.Error.OAuthError<*>)?.error) {
//            null -> false

//            is CaptchaRequiredError -> {
//                val captchaArguments = CaptchaArguments(
//                    captchaSid = error.captchaSid,
//                    captchaImage = error.captchaImage,
//                )
//
//                screenState.setValue { old ->
//                    old.copy(
//                        isNeedToOpenCaptcha = true,
//                        captchaArguments = captchaArguments
//                    )
//                }
//
//                true
//            }
//
//            is InvalidCredentialsError -> {
//                screenState.setValue { old -> old.copy(error = LoginError.WrongCredentials) }
//
//                true
//            }
//
//            is UserBannedError -> {
//                val banInfo = error.banInfo
//
//                val userBannedArguments = UserBannedArguments(
//                    name = banInfo.memberName,
//                    message = banInfo.message,
//                    restoreUrl = banInfo.restoreUrl,
//                    accessToken = banInfo.accessToken
//                )
//
//                screenState.setValue { old ->
//                    old.copy(
//                        isNeedToOpenUserBanned = true,
//                        userBannedArguments = userBannedArguments
//                    )
//                }
//
//                true
//            }
//
//            is ValidationRequiredError -> {
//                val twoFaArguments = TwoFaArguments(
//                    validationSid = error.validationSid,
//                    redirectUri = error.redirectUri,
//                    phoneMask = error.phoneMask,
//                    validationType = error.validationType,
//                    canResendSms = error.validationResend == "sms",
//                    wrongCodeError = null
//                )
//
//                screenState.setValue { old ->
//                    old.copy(
//                        isNeedToOpenTwoFa = true,
//                        twoFaArguments = twoFaArguments
//                    )
//                }
//
//                true
//            }
//
//            is WrongTwoFaCode -> {
//                screenState.setValue { old ->
//                    old.copy(
//                        isNeedToOpenTwoFa = true,
//                        twoFaArguments = old.twoFaArguments?.copy(
//                            wrongCodeError = UiText.Simple("Wrong code")
//                        )
//                    )
//                }
//
//                true
//            }
//
//            is WrongTwoFaCodeFormat -> {
//                screenState.setValue { old ->
//                    old.copy(
//                        isNeedToOpenTwoFa = true,
//                        twoFaArguments = old.twoFaArguments?.copy(
//                            wrongCodeError = UiText.Simple("Wrong code format")
//                        )
//                    )
//                }
//
//                true
//            }

//            else -> false
//        }
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
