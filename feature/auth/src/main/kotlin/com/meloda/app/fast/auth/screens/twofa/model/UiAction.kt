package com.meloda.app.fast.auth.screens.twofa.model

sealed class UiAction {

    data class CodeResult(val code: String) : UiAction()

    data object BackClicked : UiAction()

    data class CodeInputChanged(val newCode: String) : UiAction()

    data object TextFieldDoneClicked : UiAction()

    data object RequestSmsButtonClicked : UiAction()

    data object DoneButtonClicked : UiAction()
}
