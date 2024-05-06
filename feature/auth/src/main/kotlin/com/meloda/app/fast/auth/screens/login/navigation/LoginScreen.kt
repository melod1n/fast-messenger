package com.meloda.app.fast.auth.screens.login.navigation

//object LoginScreen : Screen {

//    @Composable
//    override fun Content() {
//        val navigator = LocalNavigator.currentOrThrow
//        val viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()

//        LoginScreenContent(
//            onAction = { action ->
//                when (action) {
//                    is UiAction.NavigateToCaptcha -> {
//                        navigator.push(
//                            CaptchaScreen(
//                                arguments = action.arguments,
//                                codeResult = viewModel::onCaptchaCodeReceived
//                            )
//                        )
//                    }
//
//                    is UiAction.NavigateToConversations -> {
//                        navigator.popAll()
////                        navigator.push(ConversationsScreen)
//                    }
//
//                    is UiAction.NavigateToTwoFa -> {
//                        navigator.push(
//                            TwoFaScreen(
//                                arguments = action.arguments,
//                                codeResult = viewModel::onTwoFaCodeReceived
//                            )
//                        )
//                    }
//
////                    is UiAction.NavigateToUserBanned -> {
////                        navigator.push(UserBannedScreen(arguments = action.arguments))
////                    }
//
//                    is UiAction.LoginInputChanged -> viewModel.onLoginInputChanged(action.newText)
//                    is UiAction.PasswordInputChanged -> viewModel.onPasswordInputChanged(action.newText)
//                    UiAction.PasswordVisibilityClicked -> viewModel.onPasswordVisibilityButtonClicked()
//                    UiAction.SignInClicked -> viewModel.onSignInButtonClicked()
//                }
//            },
//            viewModel = viewModel
//        )
//    }
//}
