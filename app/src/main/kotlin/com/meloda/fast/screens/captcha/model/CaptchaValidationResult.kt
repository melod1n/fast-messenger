package com.meloda.fast.screens.captcha.model

sealed class CaptchaValidationResult {
    object Empty : CaptchaValidationResult()
    object Valid : CaptchaValidationResult()

    fun isValid() = this == Valid
}
