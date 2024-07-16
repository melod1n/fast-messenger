package dev.meloda.fast.auth.captcha.validation

import dev.meloda.fast.auth.captcha.model.CaptchaScreenState
import dev.meloda.fast.auth.captcha.model.CaptchaValidationResult

class CaptchaValidator {

    fun validate(screenState: CaptchaScreenState): CaptchaValidationResult {
        return when {
            screenState.code.trim().isEmpty() -> CaptchaValidationResult.Empty
            else -> CaptchaValidationResult.Valid
        }
    }
}
