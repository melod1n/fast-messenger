package com.meloda.fast.modules.auth.screens.twofa.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.modules.auth.screens.twofa.TwoFaViewModel
import com.meloda.fast.modules.auth.screens.twofa.TwoFaViewModelImpl
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaArguments
import com.meloda.fast.modules.auth.screens.twofa.model.UiAction
import com.meloda.fast.modules.auth.screens.twofa.presentation.TwoFaRoute
import org.koin.androidx.compose.koinViewModel

data class TwoFaScreen(
    val arguments: TwoFaArguments,
    val codeResult: (String) -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: TwoFaViewModel = koinViewModel<TwoFaViewModelImpl>()
        viewModel.setArguments(arguments)

        TwoFaRoute(
            onAction = { action ->
                when (action) {
                    is UiAction.BackClicked -> navigator.pop()
                    is UiAction.CodeResult -> {
                        codeResult(action.code)
                        navigator.pop()
                    }

                    is UiAction.CodeInputChanged -> viewModel.onCodeInputChanged(action.newCode)
                    is UiAction.DoneButtonClicked -> viewModel.onDoneButtonClicked()
                    is UiAction.RequestSmsButtonClicked -> viewModel.onRequestSmsButtonClicked()
                    is UiAction.TextFieldDoneClicked -> viewModel.onTextFieldDoneClicked()
                }
            },
            viewModel = viewModel
        )
    }
}
