package com.meloda.fast.modules.auth.screens.logo.model

sealed class UiAction {

    data object Restart : UiAction()

    data object NextClicked : UiAction()
}
