package com.meloda.fast.modules.auth.screens.twofa.model

sealed class TwoFaValidationResult {
    data object Empty : TwoFaValidationResult()
    data object Valid : TwoFaValidationResult()

    fun isValid() = this == Valid
}
