package com.meloda.fast.screens.login.model

sealed class LoginValidationResult {

    data object LoginEmpty : LoginValidationResult()

    data object PasswordEmpty : LoginValidationResult()

    data object Empty : LoginValidationResult()

    data object Valid : LoginValidationResult()
}
