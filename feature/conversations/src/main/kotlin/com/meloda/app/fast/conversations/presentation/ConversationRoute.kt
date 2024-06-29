package com.meloda.app.fast.conversations.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.conversations.model.NavigationAction
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Conversations

fun NavGraphBuilder.conversationsScreen(
    onError: (BaseError) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMessagesHistory: (id: Int) -> Unit
) {
    composable<Conversations> {
        ConversationsScreen(
            onError = onError,
            onAction = { action ->
                when (action) {
                    is NavigationAction.NavigateToMessagesHistory -> {
                        onNavigateToMessagesHistory(action.conversationId)
                    }

                    NavigationAction.NavigateToSettings -> onNavigateToSettings()
                }
            }
        )
    }
}

fun NavController.navigateToConversations() {
    this.navigate(Conversations)
}
