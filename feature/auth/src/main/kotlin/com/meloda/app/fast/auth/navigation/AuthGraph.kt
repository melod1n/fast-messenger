package com.meloda.app.fast.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.meloda.app.fast.auth.screens.login.navigation.Login
import com.meloda.app.fast.auth.screens.login.navigation.loginRoute
import com.meloda.app.fast.auth.screens.twofa.navigation.TwoFa
import com.meloda.app.fast.auth.screens.twofa.navigation.setTwoFaResult
import com.meloda.app.fast.auth.screens.twofa.navigation.twoFaRoute
import com.meloda.app.fast.conversations.presentation.navigateToConversations
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object AuthGraph

fun NavGraphBuilder.authNavGraph(
    onError: (BaseError) -> Unit,
    navController: NavHostController
) {
    navigation<AuthGraph>(
        startDestination = Login
    ) {
        loginRoute(
            onError = onError,
            onNavigateToCaptcha = { arguments ->

            },
            onNavigateToTwoFa = { arguments ->
                navController.navigate(route = TwoFa(arguments))
            },
            onNavigateToConversations = navController::navigateToConversations,
            onNavigateToUserBanned = {

            }
        )

        twoFaRoute(
            onBack = navController::navigateUp,
            resultCode = { code ->
                navController.popBackStack()
                navController.setTwoFaResult(code)
            }
        )
    }
}

fun NavController.navigateToAuth() {
    this.navigate(AuthGraph)
}
