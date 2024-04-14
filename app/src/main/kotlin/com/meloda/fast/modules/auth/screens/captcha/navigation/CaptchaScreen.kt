package com.meloda.fast.modules.auth.screens.captcha.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.modules.auth.screens.captcha.model.CaptchaArguments
import com.meloda.fast.modules.auth.screens.captcha.presentation.CaptchaRoute
import com.meloda.fast.modules.auth.screens.captcha.CaptchaViewModel
import com.meloda.fast.modules.auth.screens.captcha.CaptchaViewModelImpl
import org.koin.androidx.compose.koinViewModel

// TODO: 14/04/2024, Danil Nikolaev: crash on app minimize
data class CaptchaScreen(
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
