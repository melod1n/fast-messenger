package com.meloda.fast.auth.login.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.fast.auth.login.LoginViewModel
import com.meloda.fast.auth.login.LoginViewModelImpl
import com.meloda.fast.auth.login.model.CaptchaArguments
import com.meloda.fast.auth.login.model.LoginValidationArguments
import com.meloda.fast.auth.login.model.LoginUserBannedArguments
import com.meloda.fast.auth.login.presentation.LoginRoute
import com.meloda.fast.auth.login.presentation.LogoRoute
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object Logo

fun NavGraphBuilder.loginScreen(
    onNavigateToCaptcha: (CaptchaArguments) -> Unit,
    onNavigateToValidation: (LoginValidationArguments) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToUserBanned: (LoginUserBannedArguments) -> Unit,
    onNavigateToCredentials: () -> Unit,
    navController: NavController
) {
    composable<Login> { backStackEntry ->
        val viewModel: LoginViewModel =
            backStackEntry.sharedViewModel<LoginViewModelImpl>(navController = navController)

        val validationCode = backStackEntry.getValidationResult()
        val captchaCode = backStackEntry.getCaptchaResult()

        LoginRoute(
            onNavigateToUserBanned = onNavigateToUserBanned,
            onNavigateToMain = onNavigateToMain,
            onNavigateToCaptcha = onNavigateToCaptcha,
            onNavigateToValidation = onNavigateToValidation,
            validationCode = validationCode,
            captchaCode = captchaCode,
            viewModel = viewModel
        )
    }

    composable<Logo> {
        LogoRoute(
            onNavigateToMain = onNavigateToMain,
            onGoNextButtonClicked = onNavigateToCredentials
        )
    }
}

fun NavController.navigateToLogin() {
    this.navigate(route = Login)
}

fun NavBackStackEntry.getValidationResult(): String? {
    return savedStateHandle["validation_code"]
}

fun NavBackStackEntry.getCaptchaResult(): String? {
    return savedStateHandle["captcha_code"]
}