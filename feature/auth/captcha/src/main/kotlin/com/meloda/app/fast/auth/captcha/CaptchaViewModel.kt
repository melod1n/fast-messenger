package com.meloda.app.fast.auth.captcha

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.meloda.app.fast.auth.captcha.model.CaptchaScreenState
import com.meloda.app.fast.auth.captcha.navigation.Captcha
import com.meloda.app.fast.auth.captcha.validation.CaptchaValidator
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.extensions.updateValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface CaptchaViewModel {
    val screenState: StateFlow<CaptchaScreenState>
    val isNeedToOpenLogin: StateFlow<Boolean>

    fun onCodeInputChanged(newCode: String)

    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()

    fun onNavigatedToLogin()
}

class CaptchaViewModelImpl(
    private val validator: CaptchaValidator,
    savedStateHandle: SavedStateHandle
) : CaptchaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(CaptchaScreenState.EMPTY)
    override val isNeedToOpenLogin = MutableStateFlow(false)

    init {
        val arguments = Captcha.from(savedStateHandle).arguments

        screenState.setValue { old ->
            old.copy(
                captchaSid = arguments.captchaSid,
                captchaImage = arguments.captchaImage
            )
        }
    }

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

        isNeedToOpenLogin.update { true }
    }

    override fun onNavigatedToLogin() {
        screenState.updateValue(CaptchaScreenState.EMPTY)
        isNeedToOpenLogin.update { false }
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()
        screenState.updateValue(screenState.value.copy(codeError = !isValid))
        return isValid
    }
}
