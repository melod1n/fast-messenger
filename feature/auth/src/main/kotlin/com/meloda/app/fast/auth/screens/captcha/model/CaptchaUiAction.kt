package com.meloda.app.fast.auth.screens.captcha.model

sealed class CaptchaUiAction {

    data object BackClicked : CaptchaUiAction()
    data object CancelButtonClicked : CaptchaUiAction()
    data class CodeInputChanged(val newText: String) : CaptchaUiAction()
    data object TextFieldDoneClicked : CaptchaUiAction()
    data object DoneButtonClicked : CaptchaUiAction()
    data class OnResult(val code: String) : CaptchaUiAction()
}
