package dev.meloda.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.conversations.ConversationsViewModel
import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.conversations.presentation.ConversationsRoute
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable

@Serializable
object Conversations

fun NavGraphBuilder.conversationsScreen(
    onError: (BaseError) -> Unit,
    onConversationItemClicked: (id: Int) -> Unit,
    onCreateChatClicked: () -> Unit,
    navController: NavController,
) {
    composable<Conversations> {
        val viewModel: ConversationsViewModel =
            it.sharedViewModel<ConversationsViewModelImpl>(navController = navController)

        ConversationsRoute(
            onError = onError,
            onConversationItemClicked = onConversationItemClicked,
            onCreateChatButtonClicked = onCreateChatClicked,
            viewModel = viewModel
        )
    }
}
