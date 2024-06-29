package com.meloda.app.fast.auth.captcha

import androidx.lifecycle.ViewModel
import com.meloda.app.fast.auth.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.captcha.model.CaptchaScreenState
import com.meloda.app.fast.auth.captcha.validation.CaptchaValidator
import com.meloda.app.fast.common.extensions.updateValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface CaptchaViewModel {

    val screenState: StateFlow<CaptchaScreenState>

    fun onCodeInputChanged(newCode: String)

    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()

    fun setArguments(arguments: CaptchaArguments)

    fun onNavigatedToLogin()
}

class CaptchaViewModelImpl(
    private val validator: CaptchaValidator,
) : CaptchaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(CaptchaScreenState.EMPTY)

    override fun onCodeInputChanged(newCode: String) {
        val newState = screenState.value.copy(captchaCode = newCode.trim())
        screenState.update { newState }
        processValidation()
    }

    override fun onTextFieldDoneClicked() {
        onDoneButtonClicked()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        screenState.updateValue(screenState.value.copy(isNeedToOpenLogin = true))
    }

    override fun setArguments(arguments: CaptchaArguments) {
        screenState.updateValue(
            screenState.value.copy(
                captchaSid = arguments.captchaSid,
                captchaImage = arguments.captchaImage
            )
        )
    }

    override fun onNavigatedToLogin() {
        screenState.updateValue(CaptchaScreenState.EMPTY)
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()
        screenState.updateValue(screenState.value.copy(codeError = !isValid))
        return isValid
    }
}
