package dev.meloda.fast.auth.login.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class LoginDialog {

    data object FastAuth : LoginDialog()

    data class Error(
        val errorText: String? = null,
        val errorTextResId: Int? = null
    ) : LoginDialog()
}
