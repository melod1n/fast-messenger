package com.meloda.fast.screens.captcha.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.captcha.model.CaptchaArguments
import com.meloda.fast.screens.captcha.presentation.CaptchaRoute
import com.meloda.fast.screens.captcha.presentation.CaptchaViewModel
import com.meloda.fast.screens.captcha.presentation.CaptchaViewModelImpl
import org.koin.androidx.compose.koinViewModel

data class CaptchaNavigation(
    val arguments: CaptchaArguments,
    val codeResult: (String) -> Unit
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: CaptchaViewModel = koinViewModel<CaptchaViewModelImpl>()
        viewModel.setArguments(arguments)

        CaptchaRoute(
            codeResult = codeResult,
            onBackClicked = navigator::pop,
            viewModel = viewModel
        )
    }
}
