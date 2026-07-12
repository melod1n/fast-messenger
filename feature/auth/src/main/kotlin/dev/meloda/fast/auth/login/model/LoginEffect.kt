package dev.meloda.fast.auth.login.model

sealed class LoginEffect {
    data class Navigate(val intent: LoginNavigationIntent) : LoginEffect()

    data object ClearValidationCode : LoginEffect()
}
