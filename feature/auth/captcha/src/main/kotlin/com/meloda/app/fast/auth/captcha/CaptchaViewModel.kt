package dev.meloda.fast.auth.captcha

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.meloda.fast.auth.captcha.model.CaptchaScreenState
import dev.meloda.fast.auth.captcha.navigation.Captcha
import dev.meloda.fast.auth.captcha.validation.CaptchaValidator
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.extensions.updateValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.net.URLDecoder

interface CaptchaViewModel {
    val screenState: StateFlow<CaptchaScreenState>
    val isNeedToOpenLogin: StateFlow<Boolean>

    fun onCodeInputChanged(newCode: String)

    fun onTextFieldDoneAction()
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
        val captchaImage = Captcha.from(savedStateHandle).captchaImageUrl

        screenState.setValue { old ->
            old.copy(captchaImageUrl = URLDecoder.decode(captchaImage, "utf-8"))
        }
    }

    override fun onCodeInputChanged(newCode: String) {
        val newState = screenState.value.copy(code = newCode.trim())
        screenState.update { newState }
        processValidation()
    }

    override fun onTextFieldDoneAction() {
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
