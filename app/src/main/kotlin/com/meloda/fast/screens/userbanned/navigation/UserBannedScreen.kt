package com.meloda.fast.screens.userbanned.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.login.navigation.LoginScreen
import com.meloda.fast.screens.userbanned.model.UserBannedArguments
import com.meloda.fast.screens.userbanned.presentation.UserBannedScreenContent

data class UserBannedScreen(
    val arguments: UserBannedArguments
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val isLastScreenLogin = remember {
            navigator.lastItemOrNull != null && navigator.lastItemOrNull is LoginScreen
        }

        UserBannedScreenContent(
            onBackClick = {
                if (isLastScreenLogin) {
                    navigator.pop()
                } else {
                    navigator.popAll()
                }
            },
            name = arguments.name,
            message = arguments.message,
            isLastScreenLogin = isLastScreenLogin
        )
    }
}
