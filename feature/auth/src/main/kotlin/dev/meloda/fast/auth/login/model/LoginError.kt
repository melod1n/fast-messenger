package dev.meloda.fast.auth.login.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class LoginError {
    data object Unknown : LoginError()
    data object WrongCredentials : LoginError()
    data object TooManyTries : LoginError()
    data object WrongValidationCode : LoginError()
    data object WrongValidationCodeFormat : LoginError()
    data class SimpleError(val message: String): LoginError()
}
