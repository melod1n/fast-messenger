package dev.meloda.fast.friends.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.friends.presentation.FriendsRoute
import dev.meloda.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Friends

fun NavGraphBuilder.friendsScreen(
    onError: (BaseError) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userId: Int) -> Unit,
    onScrolledToTop: () -> Unit
) {
    composable<Friends> {
        FriendsRoute(
            onError = onError,
            onPhotoClicked = onPhotoClicked,
            onMessageClicked = onMessageClicked,
            onScrolledToTop = onScrolledToTop
        )
    }
}
