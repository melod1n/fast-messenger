package com.meloda.fast.screens.twofa.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.twofa.model.TwoFaArguments
import com.meloda.fast.screens.twofa.presentation.TwoFaRoute
import com.meloda.fast.screens.twofa.presentation.TwoFaViewModel
import com.meloda.fast.screens.twofa.presentation.TwoFaViewModelImpl
import org.koin.androidx.compose.koinViewModel

data class TwoFaNavigation(
    val arguments: TwoFaArguments,
    val codeResult: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: TwoFaViewModel = koinViewModel<TwoFaViewModelImpl>()
        viewModel.setArguments(arguments)

        TwoFaRoute(
            codeResult = codeResult,
            onBackClicked = navigator::pop,
            viewModel = viewModel
        )
    }
}
