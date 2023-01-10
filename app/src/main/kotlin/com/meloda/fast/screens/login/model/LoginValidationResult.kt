package com.meloda.fast.screens.login.model

sealed class LoginValidationResult {

    object LoginEmpty : LoginValidationResult()

    object PasswordEmpty : LoginValidationResult()

    object CaptchaEmpty : LoginValidationResult()

    object ValidationEmpty : LoginValidationResult()

    object Empty : LoginValidationResult()

    object Valid : LoginValidationResult()
}
