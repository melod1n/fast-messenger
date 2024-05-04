package com.meloda.fast.screens.conversations.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.api.UserConfig
import com.meloda.fast.screens.conversations.presentation.ConversationsRoute
import com.meloda.fast.screens.messages.model.Avatar
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.navigation.MessagesHistoryScreen
import com.meloda.fast.screens.settings.navigation.SettingsScreen

object ConversationsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        ConversationsRoute(
            navigateToMessagesHistory = { conversation ->
                navigator.push(
                    MessagesHistoryScreen(
                        messagesHistoryArguments = MessagesHistoryArguments(
                            conversationId = conversation.id,
                            title = conversation.title,
                            status = conversation.lastSeenStatus,
                            avatar = when {
                                conversation.id == UserConfig.userId -> Avatar.Favorites
                                else -> conversation.avatar?.mapToAvatar() ?: Avatar.Empty
                            }
                        )
                    )
                )
            },
            navigateToSettings = {
                navigator.push(SettingsScreen)
            }
        )
    }
}
