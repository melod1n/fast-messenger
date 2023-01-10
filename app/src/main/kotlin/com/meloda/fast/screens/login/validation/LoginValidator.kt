package com.meloda.fast.screens.login.validation

import com.meloda.fast.ext.addIf
import com.meloda.fast.screens.login.model.LoginFormState
import com.meloda.fast.screens.login.model.LoginValidationResult

class LoginValidator {

    fun validate(formState: LoginFormState): List<LoginValidationResult> {
        val resultList = mutableListOf<LoginValidationResult>()

        resultList.addIf(LoginValidationResult.Empty) {
            formState == LoginFormState.EMPTY
        }

        if (resultList.isNotEmpty()) {
            return resultList
        }

        resultList.addIf(LoginValidationResult.LoginEmpty) {
            formState.login.isBlank()
        }

        resultList.addIf(LoginValidationResult.PasswordEmpty) {
            formState.password.isBlank()
        }

        resultList.addIf(LoginValidationResult.CaptchaEmpty) {
            formState.captchaSid != null && formState.captchaCode.isBlank()
        }

        resultList.addIf(LoginValidationResult.ValidationEmpty) {
            formState.validationSid != null && formState.validationCode.isBlank()
        }

        resultList.addIf(LoginValidationResult.Valid) {
            resultList.isEmpty()
        }

        return resultList
    }

}
