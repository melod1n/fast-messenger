package com.meloda.app.fast.auth.captcha.validation

import com.meloda.app.fast.auth.captcha.model.CaptchaScreenState
import com.meloda.app.fast.auth.captcha.model.CaptchaValidationResult

class CaptchaValidator {

    fun validate(screenState: CaptchaScreenState): CaptchaValidationResult {
        return when {
            screenState.captchaCode.isEmpty() -> CaptchaValidationResult.Empty
            else -> CaptchaValidationResult.Valid
        }
    }
}
