package dev.meloda.fast.auth.login.model

sealed class LoginIntent {
    data object Back : LoginIntent()

    data object LogoClicked : LoginIntent()
    data object LogoLongClicked : LoginIntent()

    data class LoginInputChange(val input: String) : LoginIntent()
    data class PasswordInputChange(val input: String) : LoginIntent()

    data object PasswordFieldEnterKeyClick : LoginIntent()
    data object PasswordFieldGoKeyClick : LoginIntent()
    data object PasswordVisibilityButtonClick : LoginIntent()

    data object SignInButtonClick : LoginIntent()

    sealed class Dialog : LoginIntent() {
        data object Dismiss : Dialog()
        data object CancelClick : Dialog()
    }
}
