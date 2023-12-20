package com.meloda.fast.screens.conversations.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.conversations.presentation.ConversationsRoute
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.navigation.MessagesHistoryScreen
import com.meloda.fast.screens.settings.navigation.SettingsScreen
import com.vk.recompose.highlighter.HighlighterModifier

object ConversationsScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ConversationsRoute(
            navigateToMessagesHistory = { conversation ->
                navigator.push(
                    MessagesHistoryScreen(
                        messagesHistoryArguments = MessagesHistoryArguments(
                            conversation = conversation
                        )
                    )
                )
            },
            navigateToSettings = {
                navigator.push(SettingsScreen)
            },
            modifier = HighlighterModifier
        )
    }
}
