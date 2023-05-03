package com.meloda.fast.screens.twofa.presentation

import androidx.lifecycle.viewModelScope
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.updateValue
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.twofa.model.TwoFaResult
import com.meloda.fast.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.screens.twofa.model.TwoFaValidationType
import com.meloda.fast.screens.twofa.screen.TwoFaCoordinator
import com.meloda.fast.screens.twofa.validation.TwoFaValidator
import kotlinx.coroutines.Job
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
}

class TwoFaViewModelImpl constructor(
    private val coordinator: TwoFaCoordinator,
    private val validator: TwoFaValidator,
    private val authRepository: AuthRepository,
    arguments: TwoFaArguments,
) : TwoFaViewModel, BaseViewModel() {

    override val screenState = MutableStateFlow(TwoFaScreenState.EMPTY)

    private var delayJob: Job? = null

    init {
        if (arguments.wrongCodeError != null) {
            screenState.updateValue(
                screenState.value.copy(codeError = arguments.wrongCodeError)
            )
        }

        screenState.updateValue(
            screenState.value.copy(
                twoFaSid = arguments.validationSid,
                twoFaText = getTwoFaText(arguments.validationType),
                canResendSms = arguments.canResendSms
            )
        )
    }

    override fun onCodeInputChanged(newCode: String) {
        screenState.updateValue(
            screenState.value.copy(
                twoFaCode = newCode.trim(),
                codeError = null
            )
        )

        if (newCode.length == 6) {
            onDoneButtonClicked()
        }
    }

    override fun onBackButtonClicked() {
        onCancelButtonClicked()
    }

    override fun onCancelButtonClicked() {
        coordinator.finishWithResult(TwoFaResult.Cancelled)
    }

    override fun onRequestSmsButtonClicked() {
        sendValidationCode()
    }

    override fun onTextFieldDoneClicked() {
        onDoneButtonClicked()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        val twoFaSid = screenState.value.twoFaSid
        val twoFaCode = screenState.value.twoFaCode

        coordinator.finishWithResult(TwoFaResult.Success(sid = twoFaSid, code = twoFaCode))
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()

        screenState.updateValue(
            screenState.value.copy(
                codeError = if (isValid) null
                else UiText.Simple("Field must not be empty")
            )
        )

        return isValid
    }

    private fun sendValidationCode() {
        val validationSid = screenState.value.twoFaSid

        viewModelScope.launch {
            sendRequest {
                authRepository.sendSms(validationSid)
            }?.let { response ->
                val newValidationType = response.validationType
                val newCanResendSms = response.validationResend == "sms"

                screenState.updateValue(
                    screenState.value.copy(
                        canResendSms = newCanResendSms,
                        twoFaText = getTwoFaText(
                            TwoFaValidationType.parse(
                                newValidationType ?: "null"
                            )
                        )
                    )
                )

                startTickTimer(response.delay)
            }
        }
    }

    private fun startTickTimer(delay: Int?) {
        if (delay == null || delayJob?.isActive.isTrue) return

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
            TwoFaValidationType.Sms -> UiText.Simple("sms")
            TwoFaValidationType.TwoFaApp -> UiText.Simple("2fa app")
            is TwoFaValidationType.Another -> UiText.Simple(validationType.type)
        }
    }
}
