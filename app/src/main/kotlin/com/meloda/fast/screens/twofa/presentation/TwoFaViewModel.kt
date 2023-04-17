package com.meloda.fast.screens.twofa.presentation

import androidx.lifecycle.viewModelScope
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.emitOnMainScope
import com.meloda.fast.ext.emitOnScope
import com.meloda.fast.ext.isTrue
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


interface TwoFaViewModel {

    val screenState: StateFlow<TwoFaScreenState>
    val delayTime: StateFlow<Int>

    val isNeedToShowCodeError: StateFlow<UiText?>

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

    override val delayTime = MutableStateFlow(0)

    override val isNeedToShowCodeError = MutableStateFlow<UiText?>(null)

    private var delayJob: Job? = null


    init {
        if (arguments.wrongCodeError != null) {
            isNeedToShowCodeError.emitOnMainScope(arguments.wrongCodeError)
        }

        val newState = screenState.value.copy(
            twoFaSid = arguments.validationSid,
            twoFaText = getTwoFaText(arguments.validationType),
            canResendSms = arguments.canResendSms
        )
        screenState.update { newState }
    }

    override fun onCodeInputChanged(newCode: String) {
        val newState = screenState.value.copy(twoFaCode = newCode.trim())
        screenState.update { newState }


        processValidation()
    }

    override fun onBackButtonClicked() {
        onCancelButtonClicked()
    }

    override fun onCancelButtonClicked() {
        clearState()
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

        clearState()
        coordinator.finishWithResult(TwoFaResult.Success(sid = twoFaSid, code = twoFaCode))
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()

        if (!isValid) {
            isNeedToShowCodeError.emitOnMainScope(UiText.Simple("Field must not be empty"))
        } else {
            isNeedToShowCodeError.emitOnMainScope(null)
        }

        return isValid
    }

    private fun clearState() {
        screenState.emitOnScope(TwoFaScreenState.EMPTY)
        isNeedToShowCodeError.emitOnMainScope(null)
    }

    private fun sendValidationCode() {
        val validationSid = screenState.value.twoFaSid

        viewModelScope.launch {
            sendRequest {
                authRepository.sendSms(validationSid)
            }?.let { response ->
                val newValidationType = response.validationType
                val newCanResendSms = response.validationResend == "sms"

                val newState = screenState.value.copy(
                    canResendSms = newCanResendSms,
                    twoFaText = getTwoFaText(
                        TwoFaValidationType.parse(
                            newValidationType ?: "null"
                        )
                    )
                )
                screenState.update { newState }

                startTickTimer(response.delay)
            }
        }
    }

    private fun startTickTimer(delay: Int?) {
        if (delay == null || delayJob?.isActive.isTrue) return

        delayJob = createTimerFlow(
            time = delay,
            onStartAction = {
                val newState = screenState.value.copy(canResendSms = false)
                screenState.update { newState }
            },
            onTickAction = delayTime::emit,
            onTimeoutAction = {
                val newState = screenState.value.copy(canResendSms = true)
                screenState.update { newState }
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
