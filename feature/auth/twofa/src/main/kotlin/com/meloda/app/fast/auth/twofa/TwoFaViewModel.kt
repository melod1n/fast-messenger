package com.meloda.app.fast.auth.twofa

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.auth.twofa.model.TwoFaScreenState
import com.meloda.app.fast.auth.twofa.model.TwoFaValidationType
import com.meloda.app.fast.auth.twofa.navigation.TwoFa
import com.meloda.app.fast.auth.twofa.validation.TwoFaValidator
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


interface TwoFaViewModel {

    val screenState: StateFlow<TwoFaScreenState>

    val isNeedToOpenLogin: StateFlow<Boolean>

    fun onCodeInputChanged(newCode: String)

    fun onBackButtonClicked()
    fun onCancelButtonClicked()
    fun onRequestSmsButtonClicked()
    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()

    fun onNavigatedToLogin()
}

class TwoFaViewModelImpl(
    private val validator: TwoFaValidator,
    private val authUseCase: AuthUseCase,
    savedStateHandle: SavedStateHandle
) : TwoFaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(TwoFaScreenState.EMPTY)

    override val isNeedToOpenLogin = MutableStateFlow(false)

    private var delayJob: Job? = null

    init {
        // TODO: 08/07/2024, Danil Nikolaev: use when fixed
        //savedStateHandle.toRoute<TwoFa>().arguments

        val arguments = TwoFa.from(savedStateHandle).arguments

        screenState.setValue { old ->
            old.copy(
                twoFaSid = arguments.validationSid,
                canResendSms = arguments.canResendSms,
                codeError = arguments.wrongCodeError,
                twoFaText = getTwoFaText(TwoFaValidationType.parse(arguments.validationType)),
                phoneMask = arguments.phoneMask
            )
        }
    }

    override fun onCodeInputChanged(newCode: String) {
        screenState.updateValue(
            screenState.value.copy(
                twoFaCode = newCode.trim(),
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
        screenState.setValue { old -> old.copy(twoFaCode = null) }
        isNeedToOpenLogin.update { true }
    }

    override fun onRequestSmsButtonClicked() {
        sendValidationCode()
    }

    override fun onTextFieldDoneClicked() {
        onDoneButtonClicked()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        isNeedToOpenLogin.update { true }
    }

    override fun onNavigatedToLogin() {
        screenState.updateValue(TwoFaScreenState.EMPTY)
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
        val validationSid = screenState.value.twoFaSid

        authUseCase.sendSms(validationSid)
            .listenValue { state ->
                state.processState(
                    error = { error ->

                    },
                    success = { response ->
                        val newValidationType = response.validationType
                        val newCanResendSms = response.validationResend == "sms"

                        screenState.setValue { old ->
                            old.copy(
                                canResendSms = newCanResendSms,
                                twoFaText = getTwoFaText(
                                    TwoFaValidationType.parse(newValidationType.orEmpty())
                                )
                            )
                        }

                        startTickTimer(response.delay)
                    }
                )

                if (state.isLoading()) {
                    screenState.emit(screenState.value.copy(canResendSms = false))
                }
            }
    }

    fun startTickTimer(delay: Int?) {
        if (delay == null || delayJob?.isActive == true) return

        delayJob = createTimerFlow(
            time = delay,
            onStartAction = {
                screenState.updateValue(
                    screenState.value.copy(canResendSms = false)
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
                        canResendSms = true
                    )
                )
            },
        ).launchIn(viewModelScope)
    }

    private fun getTwoFaText(validationType: TwoFaValidationType): UiText {
        return when (validationType) {
            TwoFaValidationType.Sms -> {
                UiText.Simple("SMS with the code is sent to ${screenState.value.phoneMask}")
            }

            TwoFaValidationType.TwoFaApp -> {
                UiText.Simple("Enter the code from the code generator application")
            }

            is TwoFaValidationType.Another -> UiText.Simple(validationType.type)
        }
    }
}
