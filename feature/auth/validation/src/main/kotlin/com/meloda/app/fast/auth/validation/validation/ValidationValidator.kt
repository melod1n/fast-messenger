package com.meloda.app.fast.auth.validation.validation

import com.meloda.app.fast.auth.validation.model.ValidationScreenState
import com.meloda.app.fast.auth.validation.model.ValidationValidationResult

class ValidationValidator {

    fun validate(screenState: ValidationScreenState): ValidationValidationResult {
        return when {
            screenState.code.isNullOrEmpty() -> ValidationValidationResult.Empty
            else -> ValidationValidationResult.Valid
        }
    }
}
