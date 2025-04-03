package dev.meloda.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.meloda.fast.conversations.ConversationsViewModel
import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.conversations.presentation.ConversationsRoute
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.ConversationsFilter
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable
import org.koin.core.qualifier.named

@Serializable
object ConversationsGraph

@Serializable
object Conversations

@Serializable
object Archive

fun NavGraphBuilder.conversationsGraph(
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (id: Long) -> Unit,
    onNavigateToCreateChat: () -> Unit,
    onScrolledToTop: () -> Unit,
    navController: NavController,
) {
    navigation<ConversationsGraph>(
        startDestination = Conversations
    ) {
        composable<Conversations> {
            val viewModel: ConversationsViewModel =
                it.sharedViewModel<ConversationsViewModelImpl>(
                    navController = navController,
                    qualifier = named(ConversationsFilter.ALL)
                )

            ConversationsRoute(
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onNavigateToCreateChat = onNavigateToCreateChat,
                onNavigateToArchive = { navController.navigate(Archive) },
                onScrolledToTop = onScrolledToTop,
                viewModel = viewModel
            )
        }
        composable<Archive> {
            val viewModel: ConversationsViewModel =
                it.sharedViewModel<ConversationsViewModelImpl>(
                    navController = navController,
                    qualifier = named(ConversationsFilter.ARCHIVE)
                )

            ConversationsRoute(
                onBack = navController::navigateUp,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onScrolledToTop = onScrolledToTop,
                viewModel = viewModel
            )
        }
    }
}
