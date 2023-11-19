package com.meloda.fast.screens.conversations.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.conversations.presentation.ConversationsRoute
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.navigation.MessagesHistoryNavigation
import com.meloda.fast.screens.settings.navigation.SettingsNavigation

object ConversationsNavigation : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ConversationsRoute(
            navigateToMessagesHistory = { conversation ->
                navigator.push(
                    MessagesHistoryNavigation(
                        messagesHistoryArguments = MessagesHistoryArguments(
                            conversation = conversation
                        )
                    )
                )
            },
            navigateToSettings = {
                navigator.push(SettingsNavigation)
            }
        )
    }
}
