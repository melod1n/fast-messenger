package com.meloda.fast.auth.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.model.BaseError
import com.meloda.fast.auth.login.model.CaptchaArguments
import com.meloda.fast.auth.login.model.TwoFaArguments
import com.meloda.fast.auth.login.model.UserBannedArguments
import com.meloda.fast.auth.login.presentation.LoginScreen
import com.meloda.fast.auth.login.presentation.LogoScreen
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Logo

fun NavGraphBuilder.loginRoute(
    onError: (BaseError) -> Unit,
    onNavigateToCaptcha: (CaptchaArguments) -> Unit,
    onNavigateToTwoFa: (TwoFaArguments) -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToUserBanned: (UserBannedArguments) -> Unit,
    onNavigateToCredentials: () -> Unit,
) {
    composable<Login> {
        LoginScreen(
            onError = onError,
            onNavigateToUserBanned = onNavigateToUserBanned,
            onNavigateToConversations = onNavigateToConversations,
            onNavigateToCaptcha = onNavigateToCaptcha,
            onNavigateToTwoFa = onNavigateToTwoFa
        )
    }

    composable<Logo> {
        LogoScreen(
            onNavigateToConversations = onNavigateToConversations,
            onShowCredentials = onNavigateToCredentials
        )
    }
}

fun NavController.navigateToLogin() {
    this.navigate(route = Login)
}
