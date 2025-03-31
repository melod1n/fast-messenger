package dev.meloda.fast.auth.login

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
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.domain.OAuthUseCase
import dev.meloda.fast.model.database.AccountEntity
import dev.meloda.fast.network.OAuthErrorDomain
import kotlinx.coroutines.CoroutineExceptionHandler
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
    val loginDialog: StateFlow<LoginDialog?>

    val validationArguments: StateFlow<LoginValidationArguments?>
    val captchaArguments: StateFlow<CaptchaArguments?>
    val userBannedArguments: StateFlow<LoginUserBannedArguments?>
    val isNeedToOpenMain: StateFlow<Boolean>

    val isNeedToClearCaptchaCode: StateFlow<Boolean>
    val isNeedToClearValidationCode: StateFlow<Boolean>

    fun onDialogConfirmed(dialog: LoginDialog, bundle: Bundle)
    fun onDialogDismissed(dialog: LoginDialog)

    fun onBackPressed()

    fun onPasswordVisibilityButtonClicked()

    fun onLoginInputChanged(newLogin: String)
    fun onPasswordInputChanged(newPassword: String)

    fun onSignInButtonClicked()

    fun onNavigatedToMain()
    fun onNavigatedToUserBanned()
    fun onNavigatedToCaptcha()
    fun onNavigatedToValidation()

    fun onValidationCodeReceived(code: String?)
    fun onValidationCodeCleared()
    fun onCaptchaCodeReceived(code: String?)
    fun onCaptchaCodeCleared()

    fun onLogoLongClicked()
}

class LoginViewModelImpl(
    private val oAuthUseCase: OAuthUseCase,
    private val authRepository: AuthRepository,
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val accountsRepository: AccountsRepository,
    private val loginValidator: LoginValidator,
    private val longPollController: LongPollController
) : ViewModel(), LoginViewModel {

    override val screenState = MutableStateFlow(LoginScreenState.EMPTY)
    override val loginDialog = MutableStateFlow<LoginDialog?>(null)

    override val validationArguments = MutableStateFlow<LoginValidationArguments?>(null)
    override val captchaArguments = MutableStateFlow<CaptchaArguments?>(null)
    override val userBannedArguments = MutableStateFlow<LoginUserBannedArguments?>(null)
    override val isNeedToOpenMain = MutableStateFlow(false)

    override val isNeedToClearCaptchaCode = MutableStateFlow(false)
    override val isNeedToClearValidationCode = MutableStateFlow(false)

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

    override fun onDialogConfirmed(dialog: LoginDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is LoginDialog.Error -> Unit

            LoginDialog.FastAuth -> {
                val token = bundle.getString("token")?.trim() ?: return
                fastAuth(token)
            }
        }
    }

    override fun onDialogDismissed(dialog: LoginDialog) {
        loginDialog.setValue { null }
    }

    override fun onBackPressed() {
        screenState.setValue { old -> old.copy(showLogo = true) }
    }

    override fun onPasswordVisibilityButtonClicked() {
        screenState.setValue { old -> old.copy(passwordVisible = !old.passwordVisible) }
    }

    override fun onLoginInputChanged(newLogin: String) {
        screenState.setValue { old ->
            old.copy(
                login = newLogin.trim(),
                loginError = false
            )
        }
    }

    override fun onPasswordInputChanged(newPassword: String) {
        screenState.setValue { old ->
            old.copy(
                password = newPassword.trim(),
                passwordError = false
            )
        }
    }

    override fun onSignInButtonClicked() {
        if (screenState.value.isLoading) return

        if (screenState.value.showLogo) {
            screenState.setValue { old -> old.copy(showLogo = false) }
            return
        }

        login()
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

    override fun onValidationCodeReceived(code: String?) {
        validationCode.update { code }
    }

    override fun onValidationCodeCleared() {
        isNeedToClearValidationCode.update { false }
    }

    override fun onCaptchaCodeReceived(code: String?) {
        captchaCode.update { code }
    }

    override fun onCaptchaCodeCleared() {
        isNeedToClearCaptchaCode.update { false }
    }

    override fun onLogoLongClicked() {
        loginDialog.setValue { LoginDialog.FastAuth }
    }

    // TODO: 31-Mar-25, Danil Nikolaev: go through full auth flow
    private fun fastAuth(token: String) {
        var currentAccount = AccountEntity(
            userId = -1,
            accessToken = token,
            fastToken = null,
            trustedHash = null,
            exchangeToken = null
        ).also { account ->
            UserConfig.currentUserId = account.userId
            UserConfig.userId = account.userId
            UserConfig.accessToken = account.accessToken
            UserConfig.fastToken = account.fastToken
            UserConfig.trustedHash = account.trustedHash
        }

        loadUserByIdUseCase(
            userId = null,
            fields = VkConstants.USER_FIELDS,
            nomCase = null
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = {
                    UserConfig.currentUserId = -1
                    UserConfig.userId = -1
                    UserConfig.accessToken = ""

                    loginDialog.setValue { LoginDialog.Error() }
                },
                success = { response ->
                    val actualUserId = requireNotNull(response).id

                    currentAccount = currentAccount.copy(userId = actualUserId)

                    UserConfig.userId = actualUserId
                    UserConfig.currentUserId = actualUserId

                    startLongPoll()

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

        screenState.updateValue { copy(isLoading = false) }

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

                    screenState.updateValue { copy(isLoading = false) }
                    captchaSid.setValue { null }

                    parseError(error)
                },
                success = { response ->
                    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                        screenState.updateValue { copy(isLoading = false) }
                        loginDialog.setValue { LoginDialog.Error() }
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
                            loginDialog.setValue { LoginDialog.Error() }
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
                                    screenState.updateValue { copy(isLoading = false) }
                                },
                                error = ::parseError,
                                success = { user ->
                                    if (user == null) {
                                        loginDialog.update { LoginDialog.Error() }
                                    } else {
                                        screenState.updateValue { copy(login = "", password = "") }
                                        isNeedToOpenMain.update { true }
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
                        validationArguments.update { arguments }
                        validationSid.update { error.validationSid }
                    }

                    is OAuthErrorDomain.CaptchaRequiredError -> {
                        val arguments = CaptchaArguments(
                            captchaSid = error.captchaSid,
                            captchaImageUrl = error.captchaImageUrl
                        )
                        captchaArguments.update { arguments }
                        captchaSid.update { error.captchaSid }
                    }

                    OAuthErrorDomain.InvalidCredentialsError -> {
                        loginDialog.setValue {
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
                        userBannedArguments.update { arguments }
                    }

                    OAuthErrorDomain.WrongValidationCode -> {
                        isNeedToClearValidationCode.update { true }
                        validationCode.update { null }
                        loginDialog.setValue {
                            LoginDialog.Error(errorText = "Wrong validation code.")
                        }
                    }

                    OAuthErrorDomain.WrongValidationCodeFormat -> {
                        isNeedToClearValidationCode.update { true }
                        validationCode.update { null }
                        loginDialog.setValue {
                            LoginDialog.Error(errorText = "Wrong validation code format.")
                        }
                    }

                    OAuthErrorDomain.TooManyTriesError -> {
                        loginDialog.setValue {
                            LoginDialog.Error(errorText = "Too many tries. Try in another hour or later.")
                        }
                    }

                    OAuthErrorDomain.UnknownError -> {
                        loginDialog.setValue { LoginDialog.Error() }
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
