package com.meloda.app.fast.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.meloda.app.fast.auth.captcha.navigation.captchaScreen
import com.meloda.app.fast.auth.captcha.navigation.navigateToCaptcha
import com.meloda.app.fast.auth.captcha.navigation.setCaptchaResult
import com.meloda.app.fast.auth.validation.model.ValidationArguments
import com.meloda.app.fast.auth.validation.navigation.navigateToValidation
import com.meloda.app.fast.auth.validation.navigation.setValidationResult
import com.meloda.app.fast.auth.validation.navigation.validationScreen
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.userbanned.model.UserBannedArguments
import com.meloda.app.fast.userbanned.navigation.navigateToUserBanned
import com.meloda.app.fast.userbanned.navigation.userBannedRoute
import com.meloda.fast.auth.login.navigation.Logo
import com.meloda.fast.auth.login.navigation.loginScreen
import com.meloda.fast.auth.login.navigation.navigateToLogin
import kotlinx.serialization.Serializable
import java.net.URLEncoder

@Serializable
object AuthGraph

fun NavGraphBuilder.authNavGraph(
    onNavigateToMain: () -> Unit,
    navController: NavController
) {
    navigation<AuthGraph>(
        startDestination = Logo
    ) {
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
                        canResendSms = arguments.canResendSms,
                        wrongCodeError = arguments.wrongCodeError
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
            onNavigateToCredentials = navController::navigateToLogin,
            navController = navController
        )

        validationScreen(
            onBack = {
                navController.navigateUp()
                navController.setValidationResult(null)
            },
            onResult = { code ->
                navController.popBackStack()
                navController.setValidationResult(code)
            }
        )

        captchaScreen(
            onBack = {
                navController.navigateUp()
                navController.setCaptchaResult(null)
            },
            onResult = { code ->
                navController.popBackStack()
                navController.setCaptchaResult(code)
            }
        )

        userBannedRoute(onBack = navController::navigateUp)
    }
}

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
