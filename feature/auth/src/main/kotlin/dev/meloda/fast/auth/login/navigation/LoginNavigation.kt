package dev.meloda.fast.auth.login.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.auth.login.LoginViewModel
import dev.meloda.fast.auth.login.model.LoginUserBannedArguments
import dev.meloda.fast.auth.login.model.LoginValidationArguments
import dev.meloda.fast.auth.login.presentation.LoginRoute
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable

@Serializable
object Login

fun NavGraphBuilder.loginScreen(
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

        LaunchedEffect(clearValidationCode) {
            if (clearValidationCode) {
                backStackEntry.savedStateHandle["validation_code"] = null
                viewModel.onValidationCodeCleared()
            }
        }

        val validationCode = backStackEntry.getValidationResult()

        LoginRoute(
            onNavigateToUserBanned = onNavigateToUserBanned,
            onNavigateToMain = onNavigateToMain,
            onNavigateToValidation = onNavigateToValidation,
            onNavigateToSettings = onNavigateToSettings,
            validationCode = validationCode,
            viewModel = viewModel
        )
    }
}

fun NavBackStackEntry.getValidationResult(): String? {
    return savedStateHandle["validation_code"]
}
