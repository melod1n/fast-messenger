package com.meloda.app.fast.auth.screens.twofa.model

sealed class TwoFaUiAction {
    data class CodeResult(val code: String) : TwoFaUiAction()
    data object BackClicked : TwoFaUiAction()
}
