package dev.meloda.fast.convos.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.convos.CreateChatViewModel
import dev.meloda.fast.convos.model.CreateChatEffect
import dev.meloda.fast.convos.model.CreateChatNavigationIntent
import dev.meloda.fast.convos.presentation.CreateChatRoute
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object CreateChat

fun NavGraphBuilder.createChatScreen(
    handleNavigationIntent: (CreateChatNavigationIntent) -> Unit,
) {
    composable<CreateChat> {
        val viewModel: CreateChatViewModel = koinViewModel()
        val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()
        val nonSelectedFriends by viewModel.nonSelectedFriendsFlow.collectAsStateWithLifecycle()
        val selectedFriends by viewModel.selectedFriendsFlow.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.screenEffectFlow.onEach { effect ->
                when (effect) {
                    is CreateChatEffect.Navigate -> handleNavigationIntent(effect.intent)
                }
            }.collect()
        }

        CreateChatRoute(
            handleIntent = viewModel::handleIntent,
            screenState = screenState,
            nonSelectedFriends = nonSelectedFriends,
            selectedFriends = selectedFriends
        )
    }
}

fun NavController.navigateToCreateChat() {
    this.navigate(CreateChat)
}
