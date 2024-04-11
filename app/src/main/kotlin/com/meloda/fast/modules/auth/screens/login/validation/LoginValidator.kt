package com.meloda.fast.modules.auth.screens.login.validation

import com.meloda.fast.ext.addIf
import com.meloda.fast.modules.auth.screens.login.model.LoginScreenState
import com.meloda.fast.modules.auth.screens.login.model.LoginValidationResult

class LoginValidator {

    fun validate(screenState: LoginScreenState): List<LoginValidationResult> {
        val resultList = mutableListOf<LoginValidationResult>()

        resultList.addIf(LoginValidationResult.LoginEmpty) {
            screenState.login.isBlank()
        }

        resultList.addIf(LoginValidationResult.PasswordEmpty) {
            screenState.password.isBlank()
        }

        resultList.addIf(LoginValidationResult.Valid) {
            resultList.isEmpty()
        }

        return resultList
    }

}
