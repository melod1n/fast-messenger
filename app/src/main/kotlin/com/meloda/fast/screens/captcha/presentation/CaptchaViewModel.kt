package com.meloda.fast.screens.captcha.presentation

import androidx.lifecycle.ViewModel
import com.meloda.fast.ext.updateValue
import com.meloda.fast.screens.captcha.model.CaptchaScreenState
import com.meloda.fast.screens.captcha.screen.CaptchaArguments
import com.meloda.fast.screens.captcha.screen.CaptchaResult
import com.meloda.fast.screens.captcha.validation.CaptchaValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface CaptchaViewModel {

    val screenState: StateFlow<CaptchaScreenState>

    fun onCodeInputChanged(newCode: String)

    fun onBackButtonClicked()
    fun onCancelButtonClicked()
    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()
}

class CaptchaViewModelImpl constructor(
    private val coordinator: CaptchaCoordinator,
    private val validator: CaptchaValidator,
    arguments: CaptchaArguments
) : CaptchaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(CaptchaScreenState.EMPTY)

    init {
        screenState.updateValue(
            screenState.value.copy(
                captchaSid = arguments.captchaSid,
                captchaImage = arguments.captchaImage
            )
        )
    }

    override fun onCodeInputChanged(newCode: String) {
        val newState = screenState.value.copy(captchaCode = newCode.trim())
        screenState.update { newState }
        processValidation()
    }

    override fun onBackButtonClicked() {
        onCancelButtonClicked()
    }

    override fun onCancelButtonClicked() {
        coordinator.finishWithResult(CaptchaResult.Cancelled)
    }

    override fun onTextFieldDoneClicked() {
        onDoneButtonClicked()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        val captchaSid = screenState.value.captchaSid
        val captchaCode = screenState.value.captchaCode

        coordinator.finishWithResult(
            CaptchaResult.Success(
                sid = captchaSid,
                code = captchaCode
            )
        )
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()
        screenState.updateValue(screenState.value.copy(codeError = !isValid))
        return isValid
    }
}
