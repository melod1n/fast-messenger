package com.meloda.app.fast.auth.screens.logo.model

sealed class LogoUiAction {

    data object Restart : LogoUiAction()

    data object NextClicked : LogoUiAction()
}
