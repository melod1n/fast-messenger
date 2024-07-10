package com.meloda.app.fast.auth

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.meloda.app.fast.auth.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.captcha.navigation.captchaRoute
import com.meloda.app.fast.auth.captcha.navigation.navigateToCaptcha
import com.meloda.app.fast.auth.captcha.navigation.setCaptchaResult
import com.meloda.app.fast.auth.twofa.model.TwoFaArguments
import com.meloda.app.fast.auth.twofa.navigation.navigateToTwoFa
import com.meloda.app.fast.auth.twofa.navigation.setTwoFaResult
import com.meloda.app.fast.auth.twofa.navigation.twoFaRoute
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.userbanned.model.UserBannedArguments
import com.meloda.app.fast.userbanned.navigation.navigateToUserBanned
import com.meloda.app.fast.userbanned.navigation.userBannedRoute
import com.meloda.fast.auth.login.navigation.Logo
import com.meloda.fast.auth.login.navigation.loginRoute
import com.meloda.fast.auth.login.navigation.navigateToLogin
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph

fun NavGraphBuilder.authNavGraph(
    onError: (BaseError) -> Unit,
    onNavigateToMain: () -> Unit,
    navController: NavHostController
) {
    navigation<AuthGraph>(
        startDestination = Logo
    ) {
        loginRoute(
            onError = onError,
            onNavigateToCaptcha = { arguments ->
                navController.navigateToCaptcha(
                    CaptchaArguments(
                        arguments.captchaSid,
                        arguments.captchaImage
                    )
                )
            },
            onNavigateToTwoFa = { arguments ->
                navController.navigateToTwoFa(
                    TwoFaArguments(
                        validationSid = arguments.validationSid,
                        redirectUri = arguments.redirectUri,
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
                        name = arguments.name,
                        message = arguments.message,
                        restoreUrl = arguments.restoreUrl,
                        accessToken = arguments.accessToken
                    )
                )
            },
            onNavigateToCredentials = navController::navigateToLogin,
            navController = navController
        )

        twoFaRoute(
            onBack = navController::navigateUp,
            onResult = { code ->
                navController.popBackStack()
                navController.setTwoFaResult(code)
            }
        )

        captchaRoute(
            onBack = navController::navigateUp,
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
