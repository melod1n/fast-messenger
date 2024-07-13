package com.meloda.app.fast.auth.validation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.auth.validation.model.ValidationScreenState
import com.meloda.app.fast.auth.validation.model.ValidationType
import com.meloda.app.fast.auth.validation.navigation.Validation
import com.meloda.app.fast.auth.validation.validation.ValidationValidator
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.extensions.createTimerFlow
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.extensions.updateValue
import com.meloda.app.fast.data.processState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


interface ValidationViewModel {

    val screenState: StateFlow<ValidationScreenState>

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

    override val isNeedToOpenLogin = MutableStateFlow(false)

    private var validationSid: String? = null

    private var delayJob: Job? = null

    init {
        // TODO: 08/07/2024, Danil Nikolaev: use when fixed
        //savedStateHandle.toRoute<Validation>().arguments

        val arguments = Validation.from(savedStateHandle).arguments

        validationSid = arguments.validationSid

        screenState.setValue { old ->
            old.copy(
                isSmsButtonVisible = arguments.canResendSms,
                codeError = arguments.wrongCodeError,
                validationText = getValidationText(ValidationType.parse(arguments.validationType)),
                phoneMask = arguments.phoneMask
            )
        }
    }

    override fun onCodeInputChanged(newCode: String) {
        screenState.updateValue(
            screenState.value.copy(
                code = newCode.trim(),
                codeError = null
            )
        )

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
        screenState.updateValue(ValidationScreenState.EMPTY)
        isNeedToOpenLogin.update { false }
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()

        screenState.updateValue(
            screenState.value.copy(
                codeError = if (isValid) null
                else "Field must not be empty"
            )
        )

        return isValid
    }

    private fun sendValidationCode() {
        authUseCase.sendSms(validationSid.orEmpty())
            .listenValue { state ->
                state.processState(
                    error = { error ->

                    },
                    success = { response ->
                        val newValidationType = response.validationType
                        val newCanResendSms = response.validationResend == "sms"

                        screenState.setValue { old ->
                            old.copy(
                                isSmsButtonVisible = newCanResendSms,
                                validationText = getValidationText(
                                    ValidationType.parse(newValidationType.orEmpty())
                                )
                            )
                        }

                        startTickTimer(response.delay)
                    }
                )

                if (state.isLoading()) {
                    screenState.emit(screenState.value.copy(isSmsButtonVisible = false))
                }
            }
    }

    fun startTickTimer(delay: Int?) {
        if (delay == null || delayJob?.isActive == true) return

        delayJob = createTimerFlow(
            time = delay,
            onStartAction = {
                screenState.updateValue(
                    screenState.value.copy(isSmsButtonVisible = false)
                )
            },
            onTickAction = { remainedTime ->
                screenState.updateValue(
                    screenState.value.copy(delayTime = remainedTime)
                )
            },
            onTimeoutAction = {
                screenState.updateValue(
                    screenState.value.copy(
                        isSmsButtonVisible = true
                    )
                )
            },
        ).launchIn(viewModelScope)
    }

    private fun getValidationText(validationType: ValidationType): UiText {
        return when (validationType) {
            ValidationType.Sms -> {
                UiText.Simple("SMS with the code is sent to ${screenState.value.phoneMask}")
            }

            ValidationType.App -> {
                UiText.Simple("Enter the code from the code generator application")
            }

            is ValidationType.Other -> UiText.Simple(validationType.type)
        }
    }
}
