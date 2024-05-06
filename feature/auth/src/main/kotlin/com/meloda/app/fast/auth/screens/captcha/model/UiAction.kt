package com.meloda.app.fast.auth.screens.captcha.model

sealed class UiAction {

    data object BackClicked : UiAction()
    data object CancelButtonClicked : UiAction()
    data class CodeInputChanged(val newText: String) : UiAction()
    data object TextFieldDoneClicked : UiAction()
    data object DoneButtonClicked : UiAction()
    data class OnResult(val code: String) : UiAction()
}
