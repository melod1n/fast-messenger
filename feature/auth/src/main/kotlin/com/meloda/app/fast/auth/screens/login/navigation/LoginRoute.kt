package com.meloda.app.fast.auth.screens.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.auth.screens.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.screens.login.model.NavigationUiAction
import com.meloda.app.fast.auth.screens.login.presentation.LoginScreen
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Login

fun NavGraphBuilder.loginRoute(
    onError: (BaseError) -> Unit,
    onNavigateToCaptcha: (CaptchaArguments) -> Unit,
    onNavigateToTwoFa: (TwoFaArguments) -> Unit,
    onNavigateToConversations: () -> Unit,
    onNavigateToUserBanned: () -> Unit,
) {
    composable<Login> {
        LoginScreen(
            onError = onError,
            onAction = { action ->
                when (action) {
                    is NavigationUiAction.NavigateToCaptcha -> {

                    }

                    NavigationUiAction.NavigateToConversations -> onNavigateToConversations()

                    is NavigationUiAction.NavigateToTwoFa -> onNavigateToTwoFa(action.arguments)

                    is NavigationUiAction.NavigateToUserBanned -> {

                    }
                }
            }
        )
    }
}

fun NavController.navigateToLogin() {
    this.navigate(route = Login)
}
