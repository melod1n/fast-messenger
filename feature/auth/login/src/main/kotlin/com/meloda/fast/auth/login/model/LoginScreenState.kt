package com.meloda.fast.auth.login.model

import androidx.compose.runtime.Immutable

// TODO: 04/05/2024, Danil Nikolaev: simplify
@Immutable
data class LoginScreenState(
    val login: String,
    val password: String,
    val captchaCode: String?,
    val validationSid: String?,
    val validationCode: String?,
    val isLoading: Boolean,
    val loginError: Boolean,
    val passwordError: Boolean,
    val passwordVisible: Boolean,
    val copiedCode: String?,
    val isNeedToNavigateToMain: Boolean,
    val twoFaArguments: LoginTwoFaArguments?,
    val captchaArguments: CaptchaArguments?,
    val userBannedArguments: UserBannedArguments?,
    val error: LoginError?,
) {

    companion object {
        val EMPTY = LoginScreenState(
            login = "",
            password = "",
            captchaCode = null,
            validationSid = null,
            validationCode = null,
            isLoading = false,
            loginError = false,
            passwordError = false,
            passwordVisible = false,
            copiedCode = null,
            isNeedToNavigateToMain = false,
            twoFaArguments = null,
            captchaArguments = null,
            userBannedArguments = null,
            error = null,
        )
    }
}
