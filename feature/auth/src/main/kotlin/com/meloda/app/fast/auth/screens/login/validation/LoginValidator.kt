package com.meloda.app.fast.auth.screens.login.validation

import com.meloda.app.fast.common.extensions.addIf
import com.meloda.app.fast.auth.screens.login.model.LoginScreenState
import com.meloda.app.fast.auth.screens.login.model.LoginValidationResult

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
