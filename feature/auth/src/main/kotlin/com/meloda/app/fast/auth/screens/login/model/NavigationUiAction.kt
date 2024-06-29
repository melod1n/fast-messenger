package com.meloda.app.fast.auth.screens.login.model

import com.meloda.app.fast.auth.screens.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.userbanned.model.UserBannedArguments

sealed class NavigationUiAction {

    data class NavigateToTwoFa(val arguments: TwoFaArguments) : NavigationUiAction()

    data class NavigateToCaptcha(val arguments: CaptchaArguments) : NavigationUiAction()

    data object NavigateToConversations : NavigationUiAction()

    data class NavigateToUserBanned(val arguments: UserBannedArguments) : NavigationUiAction()
}
