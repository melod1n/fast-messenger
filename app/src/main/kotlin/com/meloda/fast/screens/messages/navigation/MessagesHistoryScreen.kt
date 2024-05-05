package com.meloda.fast.screens.messages.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.chatmaterials.ChatMaterialsScreen
import com.meloda.fast.screens.messages.MessagesHistoryViewModel
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.model.UiAction
import com.meloda.fast.screens.messages.presentation.MessagesHistoryScreenContent
import org.koin.androidx.compose.koinViewModel

data class MessagesHistoryScreen(
    val messagesHistoryArguments: MessagesHistoryArguments
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
        viewModel.setArguments(messagesHistoryArguments)

        MessagesHistoryScreenContent(
            onAction = { action ->
                when (action) {
                    UiAction.BackClicked -> navigator.pop()

                    UiAction.OpenChatMaterials -> navigator.push(ChatMaterialsScreen)
                }
            },
            viewModel = viewModel
        )
    }
}
