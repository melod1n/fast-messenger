package dev.meloda.fast.friends.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.friends.FriendsViewModel
import dev.meloda.fast.friends.FriendsViewModelImpl
import dev.meloda.fast.friends.presentation.FriendsRoute
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.serialization.Serializable

@Serializable
object Friends

fun NavGraphBuilder.friendsScreen(
    onError: (BaseError) -> Unit,
    navController: NavController,
    onPhotoClicked: (url: String) -> Unit
) {
    composable<Friends> {
        val viewModel: FriendsViewModel =
            it.sharedViewModel<FriendsViewModelImpl>(navController = navController)

        FriendsRoute(
            onError = onError,
            viewModel = viewModel,
            onPhotoClicked = onPhotoClicked
        )
    }
}
