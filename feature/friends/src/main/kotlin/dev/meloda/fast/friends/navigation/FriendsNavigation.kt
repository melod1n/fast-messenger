package dev.meloda.fast.friends.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.friends.FriendsViewModel
import dev.meloda.fast.friends.FriendsViewModelImpl
import dev.meloda.fast.friends.presentation.FriendsRoute
import dev.meloda.fast.model.BaseError
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.ext.android.getViewModel

@Serializable
object Friends

fun NavGraphBuilder.friendsScreen(
    activity: AppCompatActivity,
    onError: (BaseError) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userId: Long) -> Unit,
    onScrolledToTop: () -> Unit
) {
    val friendsViewModel: FriendsViewModel = with(activity) {
        getViewModel<FriendsViewModelImpl>()
    }

    composable<Friends> {
        FriendsRoute(
            activity = activity,
            friendsViewModel = friendsViewModel,
            onError = onError,
            onPhotoClicked = onPhotoClicked,
            onMessageClicked = onMessageClicked,
            onScrolledToTop = onScrolledToTop
        )
    }
}
