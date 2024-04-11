package com.meloda.fast.modules.auth.screens.twofa.validation

import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaValidationResult

class TwoFaValidator {

    fun validate(screenState: TwoFaScreenState): TwoFaValidationResult {
        return when {
            screenState.twoFaCode.isNullOrEmpty() -> TwoFaValidationResult.Empty
            else -> TwoFaValidationResult.Valid
        }
    }
}
