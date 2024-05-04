package com.meloda.fast.modules.auth.screens.login.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.modules.auth.screens.captcha.navigation.CaptchaScreen
import com.meloda.fast.modules.auth.screens.login.LoginViewModel
import com.meloda.fast.modules.auth.screens.login.LoginViewModelImpl
import com.meloda.fast.modules.auth.screens.login.model.UiAction
import com.meloda.fast.modules.auth.screens.login.presentation.LoginScreenContent
import com.meloda.fast.modules.auth.screens.twofa.navigation.TwoFaScreen
import com.meloda.fast.screens.conversations.navigation.ConversationsScreen
import com.meloda.fast.screens.userbanned.navigation.UserBannedScreen
import org.koin.androidx.compose.koinViewModel

object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()

        LoginScreenContent(
            onAction = { action ->
                when (action) {
                    is UiAction.NavigateToCaptcha -> {
                        navigator.push(
                            CaptchaScreen(
                                arguments = action.arguments,
                                codeResult = viewModel::onCaptchaCodeReceived
                            )
                        )
                    }

                    is UiAction.NavigateToConversations -> {
                        navigator.popAll()
                        navigator.push(ConversationsScreen)
                    }

                    is UiAction.NavigateToTwoFa -> {
                        navigator.push(
                            TwoFaScreen(
                                arguments = action.arguments,
                                codeResult = viewModel::onTwoFaCodeReceived
                            )
                        )
                    }

                    is UiAction.NavigateToUserBanned -> {
                        navigator.push(UserBannedScreen(arguments = action.arguments))
                    }

                    is UiAction.LoginInputChanged -> viewModel.onLoginInputChanged(action.newText)
                    is UiAction.PasswordInputChanged -> viewModel.onPasswordInputChanged(action.newText)
                    UiAction.PasswordVisibilityClicked -> viewModel.onPasswordVisibilityButtonClicked()
                    UiAction.SignInClicked -> viewModel.onSignInButtonClicked()
                }
            },
            viewModel = viewModel
        )
    }
}
