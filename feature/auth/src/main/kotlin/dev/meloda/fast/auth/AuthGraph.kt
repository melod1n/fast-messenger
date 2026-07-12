package dev.meloda.fast.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import dev.meloda.fast.auth.login.model.LoginNavigationIntent
import dev.meloda.fast.auth.login.navigation.Login
import dev.meloda.fast.auth.login.navigation.loginScreen
import dev.meloda.fast.auth.userbanned.navigation.navigateToUserBanned
import dev.meloda.fast.auth.userbanned.navigation.userBannedRoute
import dev.meloda.fast.auth.validation.navigation.navigateToValidation
import dev.meloda.fast.auth.validation.navigation.setValidationResult
import dev.meloda.fast.auth.validation.navigation.validationScreen
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph

fun NavGraphBuilder.authNavGraph(
    onNavigateToMain: () -> Unit,
    onNavigateToSettings: () -> Unit,
    navController: NavController
) {
    navigation<AuthGraph>(startDestination = Login) {
        loginScreen(
            handleNavigationIntent = { intent ->
                when (intent) {
                    LoginNavigationIntent.Back -> navController.navigateUp()
                    LoginNavigationIntent.Main -> onNavigateToMain()
                    LoginNavigationIntent.Settings -> onNavigateToSettings()
                    is LoginNavigationIntent.UserBanned -> navController.navigateToUserBanned(intent.arguments)
                    is LoginNavigationIntent.Validation -> navController.navigateToValidation(intent.arguments)
                }
            },
            navController = navController
        )

        validationScreen(
            onBack = {
                navController.setValidationResult(null)
                navController.navigateUp()
            },
            onResult = { code ->
                navController.setValidationResult(code)
                navController.popBackStack()
            }
        )

        userBannedRoute(onBack = navController::navigateUp)
    }
}

// TODO: 17.12.2024, Danil Nikolaev: check clearing backstack from main screen
fun NavController.navigateToAuth(clearBackStack: Boolean = false) {
    val navController = this

    this.navigate(AuthGraph) {
        if (clearBackStack) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }
}
