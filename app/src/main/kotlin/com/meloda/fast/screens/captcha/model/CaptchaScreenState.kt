package com.meloda.fast.screens.captcha.model

data class CaptchaScreenState(
    val captchaCode: String
) {

    companion object {
        val EMPTY = CaptchaScreenState(
            captchaCode = ""
        )
    }
}
