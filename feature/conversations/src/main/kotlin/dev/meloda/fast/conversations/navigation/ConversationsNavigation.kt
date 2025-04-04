package dev.meloda.fast.conversations.navigation

import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.conversations.presentation.ConversationsRoute
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.ConversationsFilter
import dev.meloda.fast.ui.theme.LocalNavController
import dev.meloda.fast.ui.theme.getOrThrow
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
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
    onScrolledToTop: () -> Unit
) {
    navigation<ConversationsGraph>(
        startDestination = Conversations
    ) {
        composable<Conversations> {
            val context = LocalContext.current
            val navController = LocalNavController.getOrThrow()

            val viewModel: ConversationsViewModelImpl = koinViewModel(
                qualifier = named(ConversationsFilter.ALL),
                viewModelStoreOwner = context as AppCompatActivity
            )

            ConversationsRoute(
                viewModel = viewModel,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onNavigateToCreateChat = onNavigateToCreateChat,
                onNavigateToArchive = { navController.navigate(Archive) },
                onScrolledToTop = onScrolledToTop
            )
        }
        composable<Archive> {
            val context = LocalContext.current
            val navController = LocalNavController.getOrThrow()

            val viewModel: ConversationsViewModelImpl = koinViewModel(
                qualifier = named(ConversationsFilter.ARCHIVE),
                viewModelStoreOwner = context as AppCompatActivity
            )

            ConversationsRoute(
                viewModel = viewModel,
                onBack = navController::navigateUp,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onScrolledToTop = onScrolledToTop
            )
        }
    }
}
