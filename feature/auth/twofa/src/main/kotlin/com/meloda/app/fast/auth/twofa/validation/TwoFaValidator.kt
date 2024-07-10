package com.meloda.app.fast.auth.twofa.validation

import com.meloda.app.fast.auth.twofa.model.TwoFaScreenState
import com.meloda.app.fast.auth.twofa.model.TwoFaValidationResult

class TwoFaValidator {

    fun validate(screenState: TwoFaScreenState): TwoFaValidationResult {
        return when {
            screenState.twoFaCode.isNullOrEmpty() -> TwoFaValidationResult.Empty
            else -> TwoFaValidationResult.Valid
        }
    }
}
