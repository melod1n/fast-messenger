package com.meloda.app.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.app.fast.conversations.ConversationsViewModel
import com.meloda.app.fast.conversations.ConversationsViewModelImpl
import com.meloda.app.fast.conversations.presentation.ConversationsRoute
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Conversations

fun NavGraphBuilder.conversationsScreen(
    onError: (BaseError) -> Unit,
    onConversationItemClicked: (id: Int) -> Unit,
    navController: NavController,
) {
    composable<Conversations> {
        val viewModel: ConversationsViewModel =
            it.sharedViewModel<ConversationsViewModelImpl>(navController = navController)

        ConversationsRoute(
            onError = onError,
            onConversationItemClicked = onConversationItemClicked,
            viewModel = viewModel
        )
    }
}
