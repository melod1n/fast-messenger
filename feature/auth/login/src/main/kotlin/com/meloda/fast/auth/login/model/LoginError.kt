package com.meloda.fast.auth.login.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class LoginError {
    data object Unknown : LoginError()
    data object WrongCredentials : LoginError()
    data object TooManyTries : LoginError()
    data object WrongTwoFaCode : LoginError()
    data object WrongTwoFaCodeFormat : LoginError()
}
