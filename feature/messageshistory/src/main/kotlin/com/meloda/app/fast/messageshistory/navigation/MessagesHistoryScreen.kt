package com.meloda.app.fast.messageshistory.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModel
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModelImpl
import com.meloda.app.fast.messageshistory.model.MessagesHistoryArguments
import com.meloda.app.fast.messageshistory.model.UiAction
import com.meloda.app.fast.messageshistory.presentation.MessagesHistoryScreenContent
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

                    UiAction.OpenChatMaterials -> {
                        // TODO: 05/05/2024, Danil Nikolaev: add
//                        navigator.push(ChatMaterialsScreen)
                    }
                }
            },
            viewModel = viewModel
        )
    }
}
