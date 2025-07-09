package dev.meloda.fast.auth.login.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.auth.login.LoginViewModel
import dev.meloda.fast.auth.login.model.CaptchaArguments
import dev.meloda.fast.auth.login.model.LoginUserBannedArguments
import dev.meloda.fast.auth.login.model.LoginValidationArguments
import dev.meloda.fast.auth.login.presentation.LoginRoute
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable

@Serializable
object Login

fun NavGraphBuilder.loginScreen(
    onNavigateToCaptcha: (CaptchaArguments) -> Unit,
    onNavigateToValidation: (LoginValidationArguments) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToUserBanned: (LoginUserBannedArguments) -> Unit,
    onNavigateToSettings: () -> Unit,
    navController: NavController
) {
    composable<Login> { backStackEntry ->
        val viewModel: LoginViewModel =
            backStackEntry.sharedViewModel<LoginViewModel>(navController = navController)

        val clearValidationCode by viewModel.isNeedToClearValidationCode.collectAsStateWithLifecycle()
        val clearCaptchaCode by viewModel.isNeedToClearCaptchaCode.collectAsStateWithLifecycle()

        LaunchedEffect(clearValidationCode) {
            if (clearValidationCode) {
                backStackEntry.savedStateHandle["validation_code"] = null
                viewModel.onValidationCodeCleared()
            }
        }

        LaunchedEffect(clearCaptchaCode) {
            if (clearCaptchaCode) {
                backStackEntry.savedStateHandle["captcha_code"] = null
                viewModel.onCaptchaCodeCleared()
            }
        }

        val validationCode = backStackEntry.getValidationResult()
        val captchaCode = backStackEntry.getCaptchaResult()

        LoginRoute(
            onNavigateToUserBanned = onNavigateToUserBanned,
            onNavigateToMain = onNavigateToMain,
            onNavigateToCaptcha = onNavigateToCaptcha,
            onNavigateToValidation = onNavigateToValidation,
            onNavigateToSettings = onNavigateToSettings,
            validationCode = validationCode,
            captchaCode = captchaCode,
            viewModel = viewModel
        )
    }
}

fun NavBackStackEntry.getValidationResult(): String? {
    return savedStateHandle["validation_code"]
}

fun NavBackStackEntry.getCaptchaResult(): String? {
    return savedStateHandle["captcha_code"]
}
