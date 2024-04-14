package com.meloda.fast.modules.auth.screens.login.model

import com.meloda.fast.modules.auth.screens.captcha.model.CaptchaArguments
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.userbanned.model.UserBannedArguments

sealed class UiAction {

    data object Restart : UiAction()

    data class NavigateToTwoFa(val arguments: TwoFaArguments) : UiAction()

    data class NavigateToCaptcha(val arguments: CaptchaArguments) : UiAction()

    data object NavigateToConversations : UiAction()

    data class NavigateToUserBanned(val arguments: UserBannedArguments) : UiAction()

    data object SignInClicked : UiAction()

    data class LoginInputChanged(val newText: String) : UiAction()

    data class PasswordInputChanged(val newText: String) : UiAction()

    data object PasswordVisibilityClicked : UiAction()
}
