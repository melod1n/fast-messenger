package dev.meloda.fast.auth.login

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.auth.login.model.CaptchaArguments
import dev.meloda.fast.auth.login.model.LoginDialog
import dev.meloda.fast.auth.login.model.LoginEffect
import dev.meloda.fast.auth.login.model.LoginIntent
import dev.meloda.fast.auth.login.model.LoginNavigationIntent
import dev.meloda.fast.auth.login.model.LoginScreenState
import dev.meloda.fast.auth.login.model.LoginValidationResult
import dev.meloda.fast.auth.login.validation.LoginValidator
import dev.meloda.fast.auth.userbanned.model.UserBannedArguments
import dev.meloda.fast.auth.validation.model.ValidationArguments
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
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.model.AccountDto
import dev.meloda.fast.network.OAuthErrorDomain
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoginViewModel(
    private val oAuthUseCase: OAuthUseCase,
    private val authRepository: AuthRepository,
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val accountsRepository: AccountsRepository,
    private val loginValidator: LoginValidator,
    private val longPollController: LongPollController,
    private val userSettings: UserSettings,
    private val logger: FastLogger,
) : ViewModel() {

    private val screenState = MutableStateFlow(LoginScreenState.EMPTY)
    val screenStateFlow: StateFlow<LoginScreenState> get() = screenState.asStateFlow()

    private val screenEffect = MutableSharedFlow<LoginEffect>(extraBufferCapacity = 1)
    val screenEffectFlow = screenEffect.asSharedFlow()

    private val validationState: StateFlow<List<LoginValidationResult>> =
        screenState.map(loginValidator::validate)
            .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(LoginValidationResult.Empty))

    private var validationSid: String? = null

    fun onValidationCodeReceived(code: String?) {
        logger.debug(this::class, "VALIDATION CODE: $code")
        if (code != null) {
            login(code = code)
        }
    }

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            LoginIntent.Back -> onBackPressed()

            is LoginIntent.LoginInputChange -> onLoginInputChanged(intent.input)
            LoginIntent.LogoClicked -> onLogoClicked()
            LoginIntent.LogoLongClicked -> {
                screenEffect.tryEmit(LoginEffect.Navigate(LoginNavigationIntent.Settings))
            }

            is LoginIntent.PasswordInputChange -> onPasswordInputChanged(intent.input)
            LoginIntent.PasswordFieldEnterKeyClick -> login()
            LoginIntent.PasswordFieldGoKeyClick -> login()
            LoginIntent.PasswordVisibilityButtonClick -> onPasswordVisibilityButtonClicked()

            LoginIntent.SignInButtonClick -> onSignInButtonClicked()

            is LoginIntent.Dialog -> {
                when (intent) {
                    LoginIntent.Dialog.CancelClick -> Unit
                    LoginIntent.Dialog.Dismiss -> onDialogDismissed()
                }
            }
        }
    }

    private fun setDialog(dialog: LoginDialog?) {
        screenState.updateValue { copy(dialog = dialog) }
    }

    private fun onDialogDismissed() {
        val dialog = screenState.value.dialog ?: return
        when (dialog) {
            is LoginDialog.Error -> Unit
        }

        setDialog(null)
    }

    private fun onBackPressed() {
        if (screenState.value.showLogo) {
            screenEffect.tryEmit(LoginEffect.Navigate(LoginNavigationIntent.Back))
        } else {
            screenState.setValue { old ->
                old.copy(
                    showLogo = true,
                    loginError = false,
                    passwordError = false
                )
            }
        }
    }

    private fun onPasswordVisibilityButtonClicked() {
        screenState.setValue { old -> old.copy(passwordVisible = !old.passwordVisible) }
    }

    private fun onLoginInputChanged(newLogin: String) {
        screenState.setValue { old ->
            old.copy(
                login = newLogin.trim(),
                loginError = false
            )
        }
    }

    private fun onPasswordInputChanged(newPassword: String) {
        screenState.setValue { old ->
            old.copy(
                password = newPassword.trim(),
                passwordError = false
            )
        }
    }

    private fun onSignInButtonClicked() {
        if (screenState.value.isLoading) return

        if (screenState.value.showLogo) {
            screenState.setValue { old -> old.copy(showLogo = false) }
            return
        }

        login()
    }

    private fun onLogoClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            userSettings.onEnableDynamicColorsChanged(
                !userSettings.enableDynamicColors.value
            )
        }
    }

    private fun login(
        forceSms: Boolean = false,
        code: String? = null
    ) {
        val currentState = screenState.value.copy()

        processValidation()
        if (!validationState.value.contains(LoginValidationResult.Valid)) return

        screenState.updateValue { copy(isLoading = true) }

        val currentValidationSid = validationSid
        val currentValidationCode = code.takeIf { currentValidationSid != null }

        oAuthUseCase.getSilentToken(
            login = currentState.login,
            password = currentState.password,
            forceSms = forceSms,
            validationCode = currentValidationCode,
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    logger.error(this::class, "getSilentToken(): ERROR: $error")

                    screenState.updateValue { copy(isLoading = false) }

                    parseError(error)
                },
                success = { response ->
                    val exceptionHandler =
                        CoroutineExceptionHandler { _, _ ->
                            screenState.updateValue { copy(isLoading = false) }
                            setDialog(LoginDialog.Error())
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
                            screenState.updateValue { copy(isLoading = false) }
                            setDialog(LoginDialog.Error())
                            return@launch
                        }

                        val userId = exchangeSilentTokenResponse.userId
                        val accessToken = exchangeSilentTokenResponse.accessToken

                        // TODO: 30-Mar-25, Danil Nikolaev: get fast's app token

                        val currentAccount = AccountDto(
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

                        accountsRepository.storeAccounts(listOf(currentAccount.mapToEntity()))

                        startLongPoll()

                        validationSid = null

                        loadUserByIdUseCase(
                            userId = userId,
                            fields = VkConstants.USER_FIELDS,
                            nomCase = null
                        ).listenValue(viewModelScope) { state ->
                            state.processState(
                                any = {
                                    screenState.updateValue { copy(isLoading = false) }
                                },
                                error = ::parseError,
                                success = { user ->
                                    if (user == null) {
                                        setDialog(LoginDialog.Error())
                                    } else {
                                        screenState.updateValue { copy(login = "", password = "") }
                                        screenEffect.tryEmit(
                                            LoginEffect.Navigate(
                                                LoginNavigationIntent.Main
                                            )
                                        )
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
                        val arguments = ValidationArguments(
                            validationSid = error.validationSid,
                            redirectUri = error.redirectUri,
                            phoneMask = error.phoneMask,
                            validationType = error.validationType.value,
                            canResendSms = error.validationResend == "sms"
                        )
                        validationSid = error.validationSid

                        screenEffect.tryEmit(
                            LoginEffect.Navigate(LoginNavigationIntent.Validation(arguments))
                        )
                    }

                    is OAuthErrorDomain.CaptchaRequiredError -> {
                        val arguments = CaptchaArguments(
                            redirectUri = error.redirectUri
                        )
                    }

                    OAuthErrorDomain.InvalidCredentialsError -> {
                        setDialog(
                            LoginDialog.Error(errorText = "Wrong login or password.")
                        )
                    }

                    is OAuthErrorDomain.UserBannedError -> {
                        val arguments = UserBannedArguments(
                            userName = error.memberName,
                            message = error.message,
                            restoreUrl = error.restoreUrl,
                            accessToken = error.accessToken
                        )

                        screenEffect.tryEmit(
                            LoginEffect.Navigate(LoginNavigationIntent.UserBanned(arguments))
                        )
                    }

                    OAuthErrorDomain.WrongValidationCode -> {
                        screenEffect.tryEmit(LoginEffect.ClearValidationCode)
                        setDialog(
                            LoginDialog.Error(errorText = "Wrong validation code.")
                        )
                    }

                    OAuthErrorDomain.WrongValidationCodeFormat -> {
                        screenEffect.tryEmit(LoginEffect.ClearValidationCode)
                        setDialog(
                            LoginDialog.Error(errorText = "Wrong validation code format.")
                        )
                    }

                    OAuthErrorDomain.TooManyTriesError -> {
                        setDialog(
                            LoginDialog.Error(errorText = "Too many tries. Try in another hour or later.")
                        )
                    }

                    OAuthErrorDomain.UnknownError -> {
                        setDialog(
                            LoginDialog.Error()
                        )
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
                    screenState.setValue { old -> old.copy(loginError = true) }
                }

                LoginValidationResult.PasswordEmpty -> {
                    screenState.setValue { old -> old.copy(passwordError = true) }
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
