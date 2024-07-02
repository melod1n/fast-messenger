package com.meloda.app.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.conversations.presentation.ConversationsScreen
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Conversations

fun NavGraphBuilder.conversationsRoute(
    onError: (BaseError) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMessagesHistory: (id: Int) -> Unit
) {
    composable<Conversations> {
        ConversationsScreen(
            onError = onError,
            onNavigateToMessagesHistory = onNavigateToMessagesHistory,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

fun NavController.navigateToConversations() {
    val controller = this
    this.navigate(Conversations) {
        popUpTo(controller.graph.id) {
            inclusive = true
        }
    }
}
