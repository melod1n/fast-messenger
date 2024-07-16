package dev.meloda.fast.auth.validation.model

sealed class ValidationValidationResult {
    data object Empty : ValidationValidationResult()
    data object Valid : ValidationValidationResult()

    fun isValid() = this == Valid
}
