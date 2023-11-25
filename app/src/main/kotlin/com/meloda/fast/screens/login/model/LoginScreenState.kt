package com.meloda.fast.screens.login.model

import com.meloda.fast.screens.captcha.model.CaptchaArguments
import com.meloda.fast.screens.twofa.model.TwoFaArguments

data class LoginScreenState(
    val login: String,
    val password: String,
    val captchaCode: String?,
    val validationSid: String?,
    val validationCode: String?,
    val error: String?,
    val isLoading: Boolean,
    val loginError: Boolean,
    val passwordError: Boolean,
    val passwordVisible: Boolean,
    val copiedCode: String?,
    val isNeedToOpenConversations: Boolean,
    val isNeedToOpenCaptcha: Boolean,
    val isNeedToOpenTwoFa: Boolean,
    val twoFaArguments: TwoFaArguments?,
    val captchaArguments: CaptchaArguments?,
    val isNeedToRestart: Boolean
) {

    companion object {
        val EMPTY = LoginScreenState(
            login = "",
            password = "",
            captchaCode = null,
            validationSid = null,
            validationCode = null,
            error = null,
            isLoading = false,
            loginError = false,
            passwordError = false,
            passwordVisible = false,
            copiedCode = null,
            isNeedToOpenConversations = false,
            isNeedToOpenCaptcha = false,
            isNeedToOpenTwoFa = false,
            twoFaArguments = null,
            captchaArguments = null,
            isNeedToRestart = false
        )
    }
}
