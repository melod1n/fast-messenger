package com.meloda.fast.auth.login.model

import androidx.compose.runtime.Immutable

@Immutable
data class LoginScreenState(
    val login: String,
    val password: String,
    val isLoading: Boolean,
    val loginError: Boolean,
    val passwordError: Boolean,
    val passwordVisible: Boolean,
) {

    companion object {
        val EMPTY = LoginScreenState(
            login = "",
            password = "",
            isLoading = false,
            loginError = false,
            passwordError = false,
            passwordVisible = false
        )
    }
}
