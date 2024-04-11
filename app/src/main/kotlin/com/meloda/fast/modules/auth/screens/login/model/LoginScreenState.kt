package com.meloda.fast.modules.auth.screens.login.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.modules.auth.screens.captcha.model.CaptchaArguments
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.userbanned.model.UserBannedArguments

@Immutable
data class LoginScreenState(
    val isNeedToShowLogo: Boolean,
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
    val isNeedToRestart: Boolean,
    val error: LoginError?
) {

    companion object {
        val EMPTY = LoginScreenState(
            isNeedToShowLogo = true,
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
            isNeedToRestart = false,
            error = null,
        )
    }
}
