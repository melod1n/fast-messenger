package com.meloda.app.fast.auth.screens.captcha.model

sealed class CaptchaValidationResult {
    data object Empty : CaptchaValidationResult()
    data object Valid : CaptchaValidationResult()

    fun isValid() = this == Valid
}
