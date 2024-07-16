package dev.meloda.fast.auth.validation.validation

import dev.meloda.fast.auth.validation.model.ValidationScreenState
import dev.meloda.fast.auth.validation.model.ValidationValidationResult

class ValidationValidator {

    fun validate(screenState: ValidationScreenState): ValidationValidationResult {
        return when {
            screenState.code.isNullOrEmpty() -> ValidationValidationResult.Empty
            else -> ValidationValidationResult.Valid
        }
    }
}
