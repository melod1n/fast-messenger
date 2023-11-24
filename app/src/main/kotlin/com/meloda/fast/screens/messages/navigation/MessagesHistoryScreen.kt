package com.meloda.fast.screens.messages.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.messages.MessagesHistoryViewModel
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.presentation.MessagesHistoryRoute
import org.koin.androidx.compose.koinViewModel

data class MessagesHistoryScreen(
    val messagesHistoryArguments: MessagesHistoryArguments
) : AndroidScreen() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
        viewModel.setArguments(messagesHistoryArguments)

        MessagesHistoryRoute(
            onBackClicked = navigator::pop,
            viewModel = viewModel
        )
    }
}
