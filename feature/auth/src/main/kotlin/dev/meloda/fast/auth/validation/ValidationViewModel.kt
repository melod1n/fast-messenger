package dev.meloda.fast.auth.validation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.auth.validation.model.ValidationScreenState
import dev.meloda.fast.auth.validation.model.ValidationType
import dev.meloda.fast.auth.validation.navigation.Validation
import dev.meloda.fast.auth.validation.validation.ValidationValidator
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.processState
import dev.meloda.fast.domain.AuthUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


interface ValidationViewModel {

    val screenState: StateFlow<ValidationScreenState>

    val validationType: StateFlow<ValidationType?>

    val isNeedToOpenLogin: StateFlow<Boolean>

    fun onCodeInputChanged(newCode: String)

    fun onBackButtonClicked()
    fun onCancelButtonClicked()
    fun onRequestSmsButtonClicked()
    fun onTextFieldDoneAction()
    fun onDoneButtonClicked()

    fun onNavigatedToLogin()
}

class ValidationViewModelImpl(
    private val validator: ValidationValidator,
    private val authUseCase: AuthUseCase,
    savedStateHandle: SavedStateHandle
) : ValidationViewModel, ViewModel() {

    override val screenState = MutableStateFlow(ValidationScreenState.EMPTY)

    override val validationType = MutableStateFlow<ValidationType?>(null)

    override val isNeedToOpenLogin = MutableStateFlow(false)

    private var validationSid: String? = null
    private var delayJob: Job? = null

    init {
        // TODO: 08/07/2024, Danil Nikolaev: use when fixed
        //savedStateHandle.toRoute<Validation>().arguments

        val arguments = Validation.from(savedStateHandle).arguments

        validationSid = arguments.validationSid

        validationType.setValue {
            ValidationType.parse(arguments.validationType)
        }

        screenState.setValue { old ->
            old.copy(
                isSmsButtonVisible = arguments.canResendSms,
                phoneMask = arguments.phoneMask
            )
        }
    }

    override fun onCodeInputChanged(newCode: String) {
        screenState.setValue { old ->
            old.copy(
                code = newCode.trim(),
                codeError = false
            )
        }

        if (newCode.length == 6) {
            viewModelScope.launch {
                delay(250)
                onDoneButtonClicked()
            }
        }
    }

    override fun onBackButtonClicked() {
        onCancelButtonClicked()
    }

    override fun onCancelButtonClicked() {
        screenState.setValue { old -> old.copy(code = null) }
        isNeedToOpenLogin.update { true }
    }

    override fun onRequestSmsButtonClicked() {
        sendValidationCode()
    }

    override fun onTextFieldDoneAction() {
        onDoneButtonClicked()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        isNeedToOpenLogin.update { true }
    }

    override fun onNavigatedToLogin() {
        screenState.update { ValidationScreenState.EMPTY }
        isNeedToOpenLogin.update { false }
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()

        screenState.setValue { old -> old.copy(codeError = !isValid) }

        return isValid
    }

    private fun sendValidationCode() {
        val sid = validationSid ?: return

        authUseCase.validatePhone(sid)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = { error ->

                    },
                    success = { response ->
                        response.validationType?.let { newValidationType ->
                            validationType.setValue { ValidationType.parse(newValidationType) }
                        }

                        val newCanResendSms = response.validationResend == "sms"

                        screenState.setValue { old ->
                            old.copy(isSmsButtonVisible = newCanResendSms)
                        }

                        startTickTimer(response.delay)
                    }
                )

                if (state.isLoading()) {
                    screenState.emit(screenState.value.copy(isSmsButtonVisible = false))
                }
            }
    }

    private fun startTickTimer(delay: Int?) {
        if (delay == null || delayJob?.isActive == true) return

        delayJob = createTimerFlow(
            time = delay,
            onStartAction = {
                screenState.setValue { old -> old.copy(isSmsButtonVisible = false) }
            },
            onTickAction = { remainedTime ->
                screenState.setValue { old -> old.copy(delayTime = remainedTime) }
            },
            onTimeoutAction = {
                screenState.setValue { old -> old.copy(isSmsButtonVisible = true) }
            },
        ).launchIn(viewModelScope)
    }
}
