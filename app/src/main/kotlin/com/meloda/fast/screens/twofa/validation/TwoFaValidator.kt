package com.meloda.fast.screens.twofa.validation

import com.meloda.fast.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.screens.twofa.model.TwoFaValidationResult

class TwoFaValidator {

    fun validate(screenState: TwoFaScreenState): TwoFaValidationResult {
        return when {
            screenState.twoFaCode.isEmpty() -> TwoFaValidationResult.Empty
            else -> TwoFaValidationResult.Valid
        }
    }
}
