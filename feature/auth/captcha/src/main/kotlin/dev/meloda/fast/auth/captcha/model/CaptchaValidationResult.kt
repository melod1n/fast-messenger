package dev.meloda.fast.auth.captcha.model

sealed class CaptchaValidationResult {
    data object Empty : CaptchaValidationResult()
    data object Valid : CaptchaValidationResult()

    fun isValid() = this == Valid
}
