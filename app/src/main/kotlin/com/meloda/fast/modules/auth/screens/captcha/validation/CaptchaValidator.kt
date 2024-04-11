package com.meloda.fast.modules.auth.screens.captcha.validation

import com.meloda.fast.modules.auth.screens.captcha.model.CaptchaScreenState
import com.meloda.fast.modules.auth.screens.captcha.model.CaptchaValidationResult

class CaptchaValidator {

    fun validate(screenState: CaptchaScreenState): CaptchaValidationResult {
        return when {
            screenState.captchaCode.isEmpty() -> CaptchaValidationResult.Empty
            else -> CaptchaValidationResult.Valid
        }
    }
}
