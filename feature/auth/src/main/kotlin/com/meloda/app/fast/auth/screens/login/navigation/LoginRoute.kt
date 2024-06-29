package com.meloda.app.fast.auth.screens.login.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.auth.screens.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.screens.login.model.UiAction
import com.meloda.app.fast.auth.screens.login.presentation.LoginScreen
import com.meloda.app.fast.auth.screens.login.presentation.LogoScreen
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.common.extensions.restartApp
import com.meloda.app.fast.model.BaseError
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
    onNavigateToUserBanned: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    composable<Login> {
        LoginScreen(
            onError = onError,
            onAction = { action ->
                when (action) {
                    is UiAction.NavigateToCaptcha -> {

                    }

                    UiAction.NavigateToConversations -> onNavigateToConversations()

                    is UiAction.NavigateToTwoFa -> onNavigateToTwoFa(action.arguments)

                    is UiAction.NavigateToUserBanned -> {

                    }

                    else -> Unit
                }
            }
        )
    }

    composable<Logo> {
        val context = LocalContext.current
        LogoScreen(
            onAction = { action ->
                when (action) {
                    UiAction.Restart -> context.restartApp()
                    UiAction.NextClicked -> onNavigateToLogin()
                    else -> Unit
                }
            }
        )
    }
}

fun NavController.navigateToLogin() {
    this.navigate(route = Login)
}
