package com.meloda.app.fast.auth.captcha.model

data class CaptchaScreenState(
    val captchaSid: String,
    val captchaImage: String,
    val captchaCode: String,
    val codeError: Boolean
) {

    companion object {
        val EMPTY = CaptchaScreenState(
            captchaSid = "",
            captchaImage = "",
            captchaCode = "",
            codeError = false
        )
    }
}
