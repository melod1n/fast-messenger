package com.meloda.fast.screens.login.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.captcha.navigation.CaptchaScreen
import com.meloda.fast.screens.conversations.navigation.ConversationsScreen
import com.meloda.fast.screens.login.LoginViewModel
import com.meloda.fast.screens.login.LoginViewModelImpl
import com.meloda.fast.screens.login.presentation.LoginRoute
import com.meloda.fast.screens.twofa.navigation.TwoFaScreen
import org.koin.androidx.compose.koinViewModel

object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()

        LoginRoute(
            restart = {
                // TODO: 25/11/2023, Danil Nikolaev: implement
            },
            navigateToTwoFa = { arguments ->
                navigator.push(
                    TwoFaScreen(
                        arguments = arguments,
                        codeResult = viewModel::onTwoFaCodeReceived
                    )
                )
            },
            navigateToCaptcha = { arguments ->
                navigator.push(
                    CaptchaScreen(
                        arguments = arguments,
                        codeResult = viewModel::onCaptchaCodeReceived
                    )
                )
            },
            navigateToConversations = {
                navigator.popAll()
                navigator.push(ConversationsScreen)
            },
            viewModel = viewModel
        )
    }
}
