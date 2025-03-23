package dev.meloda.fast.conversations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.conversations.CreateChatViewModel
import dev.meloda.fast.conversations.CreateChatViewModelImpl
import dev.meloda.fast.conversations.presentation.CreateChatRoute
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable

@Serializable
object CreateChat

fun NavGraphBuilder.createChatScreen(
    onChatCreated: (Int) -> Unit,
    navController: NavController,
) {
    composable<CreateChat> {
        val viewModel: CreateChatViewModel =
            it.sharedViewModel<CreateChatViewModelImpl>(navController = navController)

        CreateChatRoute(
            onError = {

            },
            onBack = navController::popBackStack,
            onChatCreated = onChatCreated,
            viewModel = viewModel
        )
    }
}

fun NavController.navigateToCreateChat() {
    this.navigate(CreateChat)
}
