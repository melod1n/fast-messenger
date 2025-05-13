package dev.meloda.fast.auth.login.validation

import dev.meloda.fast.common.extensions.addIf
import dev.meloda.fast.auth.login.model.LoginScreenState
import dev.meloda.fast.auth.login.model.LoginValidationResult

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
