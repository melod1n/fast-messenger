package com.meloda.fast.screens.captcha

import androidx.lifecycle.ViewModel
import com.github.terrakok.cicerone.Router
import com.meloda.fast.screens.captcha.model.CaptchaScreenState
import com.meloda.fast.screens.captcha.screen.CaptchaResult
import com.meloda.fast.screens.captcha.validation.CaptchaValidator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface CaptchaViewModel {

    val screenState: StateFlow<CaptchaScreenState>

    val isNeedToShowCodeError: StateFlow<Boolean>

    fun onCodeInputChanged(newCode: String)

    fun onBackButtonClicked()
    fun onCancelButtonClicked()
    fun onTextFieldDoneClicked()
    fun onDoneButtonClicked()
}

class CaptchaViewModelImpl constructor(
    private val resultFlow: MutableSharedFlow<CaptchaResult>,
    private val router: Router,
    private val validator: CaptchaValidator
) : CaptchaViewModel, ViewModel() {

    override val screenState = MutableStateFlow(CaptchaScreenState.EMPTY)

    override val isNeedToShowCodeError = MutableStateFlow(false)

    override fun onCodeInputChanged(newCode: String) {
        val newState = screenState.value.copy(captchaCode = newCode.trim())
        screenState.update { newState }
        processValidation()
    }

    override fun onBackButtonClicked() {
        onCancelButtonClicked()
    }

    override fun onCancelButtonClicked() {
        clearState()
        finishWithResult(CaptchaResult.Cancelled)
    }

    override fun onTextFieldDoneClicked() {
        processValidation()
    }

    override fun onDoneButtonClicked() {
        if (!processValidation()) return

        val captchaCode = screenState.value.captchaCode

        clearState()
        finishWithResult(CaptchaResult.Success(captchaCode))
    }

    private fun processValidation(): Boolean {
        val isValid = validator.validate(screenState.value).isValid()
        isNeedToShowCodeError.tryEmit(!isValid)
        return isValid
    }

    private fun clearState() {
        screenState.tryEmit(CaptchaScreenState.EMPTY)
        isNeedToShowCodeError.tryEmit(false)
    }

    private fun finishWithResult(result: CaptchaResult) {
        resultFlow.tryEmit(result)
        router.exit()
    }
}
