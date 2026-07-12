package dev.meloda.fast.auth.login.model

import dev.meloda.fast.auth.userbanned.model.UserBannedArguments
import dev.meloda.fast.auth.validation.model.ValidationArguments

sealed class LoginNavigationIntent {
    data object Back : LoginNavigationIntent()

    data class Validation(val arguments: ValidationArguments) : LoginNavigationIntent()
    data class UserBanned(val arguments: UserBannedArguments) : LoginNavigationIntent()
    data object Main : LoginNavigationIntent()
    data object Settings: LoginNavigationIntent()
}
