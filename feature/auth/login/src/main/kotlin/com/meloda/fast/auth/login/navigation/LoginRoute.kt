package com.meloda.fast.auth.login.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.app.fast.model.BaseError
import com.meloda.fast.auth.login.LoginViewModel
import com.meloda.fast.auth.login.LoginViewModelImpl
import com.meloda.fast.auth.login.model.LoginCaptchaArguments
import com.meloda.fast.auth.login.model.LoginTwoFaArguments
import com.meloda.fast.auth.login.model.LoginUserBannedArguments
import com.meloda.fast.auth.login.presentation.LoginScreen
import com.meloda.fast.auth.login.presentation.LogoScreen
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Logo

fun NavGraphBuilder.loginRoute(
    onError: (BaseError) -> Unit,
    onNavigateToCaptcha: (LoginCaptchaArguments) -> Unit,
    onNavigateToTwoFa: (LoginTwoFaArguments) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToUserBanned: (LoginUserBannedArguments) -> Unit,
    onNavigateToCredentials: () -> Unit,
    navController: NavController
) {
    composable<Login> { backStackEntry ->
        val viewModel: LoginViewModel =
            backStackEntry.sharedViewModel<LoginViewModelImpl>(navController = navController)

        val twoFaCode = backStackEntry.getTwoFaResult()
        val captchaCode = backStackEntry.getCaptchaResult()

        LoginScreen(
            onError = onError,
            onNavigateToUserBanned = onNavigateToUserBanned,
            onNavigateToMain = onNavigateToMain,
            onNavigateToCaptcha = onNavigateToCaptcha,
            onNavigateToTwoFa = onNavigateToTwoFa,
            twoFaCode = twoFaCode,
            captchaCode = captchaCode,
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

fun NavBackStackEntry.getTwoFaResult(): String? {
    return savedStateHandle["twofacode"]
}

fun NavBackStackEntry.getCaptchaResult(): String? {
    return savedStateHandle["captchacode"]
}
