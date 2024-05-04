package com.meloda.fast.modules.auth.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.CaptchaRequiredError
import com.meloda.fast.api.network.InvalidCredentialsError
import com.meloda.fast.api.network.UserBannedError
import com.meloda.fast.api.network.ValidationRequiredError
import com.meloda.fast.api.network.WrongTwoFaCode
import com.meloda.fast.api.network.WrongTwoFaCodeFormat
import com.meloda.fast.base.State
import com.meloda.fast.base.processState
import com.meloda.fast.data.users.domain.UsersUseCase
import com.meloda.fast.database.dao.AccountsDao
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.setValue
import com.meloda.fast.ext.updateValue
import com.meloda.fast.model.AppAccount
import com.meloda.fast.model.base.UiText
import com.meloda.fast.modules.auth.model.domain.usecase.OAuthUseCase
import com.meloda.fast.modules.auth.screens.captcha.model.CaptchaArguments
import com.meloda.fast.modules.auth.screens.login.model.LoginError
import com.meloda.fast.modules.auth.screens.login.model.LoginScreenState
import com.meloda.fast.modules.auth.screens.login.model.LoginValidationResult
import com.meloda.fast.modules.auth.screens.login.validation.LoginValidator
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.userbanned.model.UserBannedArguments
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

interface LoginViewModel {
    val screenState: StateFlow<LoginScreenState>

    fun onPasswordVisibilityButtonClicked()

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)

    fun onSignInButtonClicked()

    fun onErrorDialogDismissed()

    fun onNavigatedToConversations()

    fun onNavigatedToUserBanned()

    fun onTwoFaCodeReceived(code: String)
    fun onCaptchaCodeReceived(code: String)
}

class LoginViewModelImpl(
    private val oAuthUseCase: OAuthUseCase,
    private val usersUseCase: UsersUseCase,
    private val accounts: AccountsDao,
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

    override fun onNavigatedToConversations() {
        screenState.updateValue(screenState.value.copy(isNeedToOpenConversations = false))
    }

    override fun onNavigatedToUserBanned() {
        screenState.setValue { old ->
            old.copy(
                isNeedToOpenUserBanned = false,
                userBannedArguments = null
            )
        }
    }

    override fun onTwoFaCodeReceived(code: String) {
        screenState.updateValue(
            screenState.value.copy(
                isNeedToOpenTwoFa = false,
                validationCode = code
            )
        )

        login()
    }

    override fun onCaptchaCodeReceived(code: String) {
        screenState.updateValue(
            screenState.value.copy(
                isNeedToOpenCaptcha = false,
                captchaCode = code
            )
        )

        login()
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

                    usersUseCase.getUserById(
                        userId = userId,
                        fields = VKConstants.USER_FIELDS,
                        nomCase = null
                    ).listenValue { state ->
                        state.processState(
                            error = {},
                            success = { user -> user?.let { usersUseCase.storeUser(user) } }
                        )
                    }

                    val currentAccount = AppAccount(
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

                    accounts.insert(listOf(currentAccount))

                    screenState.setValue { old ->
                        old.copy(
                            captchaArguments = null,
                            captchaCode = null,
                            validationSid = null,
                            validationCode = null,
                            twoFaArguments = null,

                            login = "",
                            password = "",

                            isNeedToOpenConversations = true
                        )
                    }
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun parseError(stateError: State.Error): Boolean {
        return when (val error = (stateError as? State.Error.OAuthError<*>)?.error) {
            null -> false

            is CaptchaRequiredError -> {
                val captchaArguments = CaptchaArguments(
                    captchaSid = error.captchaSid,
                    captchaImage = error.captchaImage,
                )

                screenState.setValue { old ->
                    old.copy(
                        isNeedToOpenCaptcha = true,
                        captchaArguments = captchaArguments
                    )
                }

                true
            }

            is InvalidCredentialsError -> {
                screenState.setValue { old -> old.copy(error = LoginError.WrongCredentials) }

                true
            }

            is UserBannedError -> {
                val banInfo = error.banInfo

                val userBannedArguments = UserBannedArguments(
                    name = banInfo.memberName,
                    message = banInfo.message,
                    restoreUrl = banInfo.restoreUrl,
                    accessToken = banInfo.accessToken
                )

                screenState.setValue { old ->
                    old.copy(
                        isNeedToOpenUserBanned = true,
                        userBannedArguments = userBannedArguments
                    )
                }

                true
            }

            is ValidationRequiredError -> {
                val twoFaArguments = TwoFaArguments(
                    validationSid = error.validationSid,
                    redirectUri = error.redirectUri,
                    phoneMask = error.phoneMask,
                    validationType = error.validationType,
                    canResendSms = error.validationResend == "sms",
                    wrongCodeError = null
                )

                screenState.setValue { old ->
                    old.copy(
                        isNeedToOpenTwoFa = true,
                        twoFaArguments = twoFaArguments
                    )
                }

                true
            }

            is WrongTwoFaCode -> {
                screenState.setValue { old ->
                    old.copy(
                        isNeedToOpenTwoFa = true,
                        twoFaArguments = old.twoFaArguments?.copy(
                            wrongCodeError = UiText.Simple("Wrong code")
                        )
                    )
                }

                true
            }

            is WrongTwoFaCodeFormat -> {
                screenState.setValue { old ->
                    old.copy(
                        isNeedToOpenTwoFa = true,
                        twoFaArguments = old.twoFaArguments?.copy(
                            wrongCodeError = UiText.Simple("Wrong code format")
                        )
                    )
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
