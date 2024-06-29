package com.meloda.app.fast.auth.screens.twofa

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.app.fast.auth.AuthUseCase
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaScreenState
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaValidationType
import com.meloda.app.fast.auth.screens.twofa.validation.TwoFaValidator
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
import kotlinx.coroutines.launch


interface TwoFaViewModel {

    val screenState: StateFlow<TwoFaScreenState>

    fun onCodeInputChanged(newCode: String)

    fun onBackButtonClicked()
    fun onCancelButtonClicked()
    fun onRequestSmsButtonClicked()
    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()

    fun onNavigatedToLogin()

    fun setArguments(arguments: TwoFaArguments)
}

class TwoFaViewModelImpl(
    private val validator: TwoFaValidator,
    private val authUseCase: AuthUseCase,
    savedStateHandle: SavedStateHandle
) : TwoFaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(TwoFaScreenState.EMPTY)

    private var delayJob: Job? = null

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
        screenState.updateValue(
            screenState.value.copy(
                twoFaCode = null,
                isNeedToOpenLogin = true
            )
        )
    }

    override fun onRequestSmsButtonClicked() {
        sendValidationCode()
    }

    override fun onTextFieldDoneClicked() {
        onDoneButtonClicked()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        screenState.updateValue(screenState.value.copy(isNeedToOpenLogin = true))
    }

    override fun onNavigatedToLogin() {
        screenState.updateValue(TwoFaScreenState.EMPTY)
    }

    override fun setArguments(arguments: TwoFaArguments) {
        Log.d("TwoFaViewModel", "TwoFaArguments: $arguments")

        screenState.updateValue(
            screenState.value.copy(
                twoFaSid = arguments.validationSid,
                canResendSms = arguments.canResendSms,
                codeError = arguments.wrongCodeError,
                twoFaText = getTwoFaText(TwoFaValidationType.parse(arguments.validationType)),
                phoneMask = arguments.phoneMask
            )
        )
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
                    error = { error -> },
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
