package com.meloda.app.fast.friends.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.app.fast.friends.FriendsViewModel
import com.meloda.app.fast.friends.FriendsViewModelImpl
import com.meloda.app.fast.friends.presentation.FriendsScreen
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable

@Serializable
object Friends

fun NavGraphBuilder.friendsRoute(
    onError: (BaseError) -> Unit,
    navController: NavController
) {
    composable<Friends> {
        val viewModel: FriendsViewModel =
            it.sharedViewModel<FriendsViewModelImpl>(navController = navController)

        FriendsScreen(
            onError = onError,
            viewModel = viewModel
        )
    }
}
