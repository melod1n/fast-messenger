package com.meloda.app.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.app.fast.conversations.ConversationsViewModel
import com.meloda.app.fast.conversations.ConversationsViewModelImpl
import com.meloda.app.fast.conversations.presentation.ConversationsScreen
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Conversations

fun NavGraphBuilder.conversationsRoute(
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (id: Int) -> Unit,
    onListScrollingUp: (Boolean) -> Unit,
    navController: NavController,
) {
    composable<Conversations> {
        val viewModel: ConversationsViewModel =
            it.sharedViewModel<ConversationsViewModelImpl>(navController = navController)

        ConversationsScreen(
            onError = onError,
            onNavigateToMessagesHistory = onNavigateToMessagesHistory,
            onListScrollingUp = onListScrollingUp,
            viewModel = viewModel
        )
    }
}
