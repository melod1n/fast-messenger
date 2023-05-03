package com.meloda.fast.screens.twofa.model

sealed class TwoFaValidationResult {
    object Empty : TwoFaValidationResult()
    object Valid : TwoFaValidationResult()

    fun isValid() = this == Valid
}
