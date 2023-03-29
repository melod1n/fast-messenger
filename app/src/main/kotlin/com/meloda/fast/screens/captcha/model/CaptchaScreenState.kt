package com.meloda.fast.screens.captcha.model

data class CaptchaScreenState(
    val captchaSid: String,
    val captchaCode: String
) {

    companion object {
        val EMPTY = CaptchaScreenState(
            captchaSid = "",
            captchaCode = ""
        )
    }
}
