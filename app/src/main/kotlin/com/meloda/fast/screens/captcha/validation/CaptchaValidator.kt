package com.meloda.fast.screens.captcha.validation

import com.meloda.fast.screens.captcha.model.CaptchaScreenState
import com.meloda.fast.screens.captcha.model.CaptchaValidationResult

class CaptchaValidator {

    fun validate(screenState: CaptchaScreenState): CaptchaValidationResult {
        return when {
            screenState.captchaCode.isEmpty() -> CaptchaValidationResult.Empty
            else -> CaptchaValidationResult.Valid
        }
    }
}
