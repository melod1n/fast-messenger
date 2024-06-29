package com.meloda.app.fast.settings.model

sealed class NavigationAction {

    data object BackClick : NavigationAction()
    data object NavigateToLanguagePicker : NavigationAction()
    data object NavigateToLogin : NavigationAction()
}
