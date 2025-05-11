package dev.meloda.fast.conversations.navigation

import androidx.appcompat.app.AppCompatActivity
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
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.qualifier.named

@Serializable
object ConversationsGraph

@Serializable
object Conversations

@Serializable
object Archive

fun NavGraphBuilder.conversationsGraph(
    activity: AppCompatActivity,
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (id: Long) -> Unit,
    onNavigateToCreateChat: () -> Unit,
    onScrolledToTop: () -> Unit
) {
    navigation<ConversationsGraph>(
        startDestination = Conversations
    ) {
        val conversationsViewModel: ConversationsViewModelImpl = with(activity) {
            getViewModel(qualifier = named(ConversationsFilter.ALL))
        }
        val archiveViewModel: ConversationsViewModelImpl = with(activity) {
            getViewModel(qualifier = named(ConversationsFilter.ARCHIVE))
        }

        composable<Conversations> {
            val navController = LocalNavController.getOrThrow()

            ConversationsRoute(
                viewModel = conversationsViewModel,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onNavigateToCreateChat = onNavigateToCreateChat,
                onNavigateToArchive = { navController.navigate(Archive) },
                onScrolledToTop = onScrolledToTop
            )
        }
        composable<Archive> {
            val navController = LocalNavController.getOrThrow()

            ConversationsRoute(
                viewModel = archiveViewModel,
                onBack = navController::navigateUp,
                onError = onError,
                onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                onScrolledToTop = onScrolledToTop
            )
        }
    }
}
