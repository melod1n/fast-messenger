package dev.meloda.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.conversations.ConversationsViewModel
import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.conversations.presentation.ConversationsRoute
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.ConversationFilter
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named

@Serializable
object Conversations

fun NavGraphBuilder.conversationsScreen(
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (id: Long) -> Unit,
    onNavigateToCreateChat: () -> Unit,
    onScrolledToTop: () -> Unit,
    navController: NavController,
) {
    composable<Conversations> {
        val viewModel: ConversationsViewModel =
            it.sharedViewModel<ConversationsViewModelImpl>(
                navController = navController,
                qualifier = named(ConversationFilter.ALL)
            )

        ConversationsRoute(
            onError = onError,
            onNavigateToMessagesHistory = onNavigateToMessagesHistory,
            onNavigateToCreateChat = onNavigateToCreateChat,
            onScrolledToTop = onScrolledToTop,
            viewModel = viewModel
        )
    }
}
