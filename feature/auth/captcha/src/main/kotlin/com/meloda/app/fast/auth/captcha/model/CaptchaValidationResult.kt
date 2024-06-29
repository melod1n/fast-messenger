package com.meloda.app.fast.auth.captcha.model

sealed class CaptchaValidationResult {
    data object Empty : CaptchaValidationResult()
    data object Valid : CaptchaValidationResult()

    fun isValid() = this == Valid
}
