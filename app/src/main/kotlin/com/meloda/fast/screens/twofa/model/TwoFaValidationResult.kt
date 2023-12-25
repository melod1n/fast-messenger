package com.meloda.fast.screens.twofa.model

sealed class TwoFaValidationResult {
    data object Empty : TwoFaValidationResult()
    data object Valid : TwoFaValidationResult()

    fun isValid() = this == Valid
}
