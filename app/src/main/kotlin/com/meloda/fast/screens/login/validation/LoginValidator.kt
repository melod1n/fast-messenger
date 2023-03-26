package com.meloda.fast.screens.login.validation

import com.meloda.fast.ext.addIf
import com.meloda.fast.screens.login.model.LoginScreenState
import com.meloda.fast.screens.login.model.LoginValidationResult

class LoginValidator {

    fun validate(formState: LoginScreenState): List<LoginValidationResult> {
        val resultList = mutableListOf<LoginValidationResult>()

        resultList.addIf(LoginValidationResult.LoginEmpty) {
            formState.login.isBlank()
        }

        resultList.addIf(LoginValidationResult.PasswordEmpty) {
            formState.password.isBlank()
        }

        resultList.addIf(LoginValidationResult.CaptchaEmpty) {
            formState.captchaSid != null && formState.captchaCode.isNullOrBlank()
        }

        resultList.addIf(LoginValidationResult.ValidationEmpty) {
            formState.validationSid != null && formState.validationCode.isNullOrBlank()
        }

        resultList.addIf(LoginValidationResult.Valid) {
            resultList.isEmpty()
        }

        return resultList
    }

}
