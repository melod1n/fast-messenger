package com.meloda.app.fast.auth.screens.logo.model

sealed class UiAction {

    data object Restart : UiAction()

    data object NextClicked : UiAction()
}
