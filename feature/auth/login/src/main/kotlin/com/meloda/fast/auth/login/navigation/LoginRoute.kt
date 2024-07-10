package com.meloda.fast.auth.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.app.fast.model.BaseError
import com.meloda.fast.auth.login.LoginViewModel
import com.meloda.fast.auth.login.LoginViewModelImpl
import com.meloda.fast.auth.login.model.CaptchaArguments
import com.meloda.fast.auth.login.model.LoginTwoFaArguments
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
    onNavigateToTwoFa: (LoginTwoFaArguments) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToUserBanned: (UserBannedArguments) -> Unit,
    onNavigateToCredentials: () -> Unit,
    navController: NavController
) {
    composable<Login> {
        val viewModel: LoginViewModel =
            it.sharedViewModel<LoginViewModelImpl>(navController = navController)

        LoginScreen(
            onError = onError,
            onNavigateToUserBanned = onNavigateToUserBanned,
            onNavigateToMain = onNavigateToMain,
            onNavigateToCaptcha = onNavigateToCaptcha,
            onNavigateToTwoFa = onNavigateToTwoFa,
            viewModel = viewModel
        )
    }

    composable<Logo> {
        LogoScreen(
            onNavigateToMain = onNavigateToMain,
            onShowCredentials = onNavigateToCredentials
        )
    }
}

fun NavController.navigateToLogin() {
    this.navigate(route = Login)
}
