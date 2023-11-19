package com.meloda.fast.screens.login.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.captcha.navigation.CaptchaNavigation
import com.meloda.fast.screens.conversations.navigation.ConversationsNavigation
import com.meloda.fast.screens.login.LoginViewModel
import com.meloda.fast.screens.login.LoginViewModelImpl
import com.meloda.fast.screens.login.presentation.LoginRoute
import com.meloda.fast.screens.twofa.navigation.TwoFaNavigation
import org.koin.androidx.compose.koinViewModel

object LoginNavigation : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()

        LoginRoute(
            navigateToTwoFa = { arguments ->
                navigator.push(
                    TwoFaNavigation(
                        arguments = arguments,
                        codeResult = viewModel::onTwoFaCodeReceived
                    )
                )
            },
            navigateToCaptcha = { arguments ->
                navigator.push(
                    CaptchaNavigation(
                        arguments = arguments,
                        codeResult = viewModel::onCaptchaCodeReceived
                    )
                )
            },
            navigateToConversations = {
                navigator.popAll()
                navigator.push(ConversationsNavigation)
            },
            viewModel = viewModel
        )
    }
}
