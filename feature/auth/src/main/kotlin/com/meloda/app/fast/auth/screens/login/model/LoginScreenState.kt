package com.meloda.app.fast.auth.screens.login.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.auth.screens.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.userbanned.model.UserBannedArguments

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
    val isNeedToOpenConversations: Boolean,
    val isNeedToOpenCaptcha: Boolean,
    val isNeedToOpenTwoFa: Boolean,
    val isNeedToOpenUserBanned: Boolean,
    val twoFaArguments: TwoFaArguments?,
    val captchaArguments: CaptchaArguments?,
    val userBannedArguments: UserBannedArguments?,
    val error: LoginError?,
    val isNeedToRestart: Boolean
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
            isNeedToOpenConversations = false,
            isNeedToOpenCaptcha = false,
            isNeedToOpenTwoFa = false,
            isNeedToOpenUserBanned = false,
            twoFaArguments = null,
            captchaArguments = null,
            userBannedArguments = null,
            error = null,
            isNeedToRestart = false
        )
    }
}
