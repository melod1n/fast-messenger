package com.meloda.app.fast.auth.captcha.model

data class CaptchaScreenState(
    val captchaImageUrl: String,
    val code: String,
    val codeError: Boolean
) {

    companion object {
        val EMPTY = CaptchaScreenState(
            captchaImageUrl = "",
            code = "",
            codeError = false
        )
    }
}
