package dev.meloda.fast.convos.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.convos.CreateChatViewModel
import dev.meloda.fast.convos.presentation.CreateChatRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object CreateChat

fun NavGraphBuilder.createChatScreen(
    onChatCreated: (Long) -> Unit,
    navController: NavController,
) {
    composable<CreateChat> {
        val context = LocalContext.current
        val viewModel: CreateChatViewModel = koinViewModel(
            viewModelStoreOwner = context as AppCompatActivity
        )

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
