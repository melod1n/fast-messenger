package com.meloda.fast.screens.messages.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.chatmaterials.ChatMaterials
import com.meloda.fast.screens.messages.MessagesHistoryViewModel
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.presentation.MessagesHistoryRoute
import org.koin.androidx.compose.koinViewModel

// TODO: 14/04/2024, Danil Nikolaev: crash on app minimize
data class MessagesHistoryScreen(
    val messagesHistoryArguments: MessagesHistoryArguments
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
        viewModel.setArguments(messagesHistoryArguments)

        MessagesHistoryRoute(
            openChatMaterials = {
                navigator.push(ChatMaterials)
            },
            onBackClicked = navigator::pop,
            viewModel = viewModel
        )
    }
}
