package dev.meloda.fast.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import dev.meloda.fast.auth.captcha.navigation.captchaScreen
import dev.meloda.fast.auth.captcha.navigation.navigateToCaptcha
import dev.meloda.fast.auth.captcha.navigation.setCaptchaResult
import dev.meloda.fast.auth.login.navigation.Login
import dev.meloda.fast.auth.login.navigation.loginScreen
import dev.meloda.fast.auth.userbanned.model.UserBannedArguments
import dev.meloda.fast.auth.userbanned.navigation.navigateToUserBanned
import dev.meloda.fast.auth.userbanned.navigation.userBannedRoute
import dev.meloda.fast.auth.validation.model.ValidationArguments
import dev.meloda.fast.auth.validation.navigation.navigateToValidation
import dev.meloda.fast.auth.validation.navigation.setValidationResult
import dev.meloda.fast.auth.validation.navigation.validationScreen
import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
object AuthGraph

fun NavGraphBuilder.authNavGraph(
    onNavigateToMain: () -> Unit,
    onNavigateToSettings: () -> Unit,
    navController: NavController
) {
    navigation<AuthGraph>(startDestination = Login) {
        loginScreen(
            onNavigateToCaptcha = { arguments ->
                navController.navigateToCaptcha(
                    captchaImageUrl = URLEncoder.encode(arguments.captchaImageUrl, "utf-8")
                )
            },
            onNavigateToValidation = { arguments ->
                navController.navigateToValidation(
                    ValidationArguments(
                        validationSid = arguments.validationSid,
                        redirectUri = URLEncoder.encode(arguments.redirectUri, "utf-8"),
                        phoneMask = arguments.phoneMask,
                        validationType = arguments.validationType,
                        canResendSms = arguments.canResendSms
                    )
                )
            },
            onNavigateToMain = onNavigateToMain,
            onNavigateToUserBanned = { arguments ->
                navController.navigateToUserBanned(
                    UserBannedArguments(
                        userName = arguments.name,
                        message = arguments.message,
                        restoreUrl = arguments.restoreUrl,
                        accessToken = arguments.accessToken
                    )
                )
            },
            onNavigateToSettings = onNavigateToSettings,
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

        captchaScreen(
            onBack = {
                navController.setCaptchaResult(null)
                navController.navigateUp()
            },
            onResult = { code ->
                navController.setCaptchaResult(code)
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
