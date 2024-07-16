package dev.meloda.fast.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.common.extensions.navigation.sharedViewModel
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.profile.ProfileViewModel
import dev.meloda.fast.profile.ProfileViewModelImpl
import dev.meloda.fast.profile.presentation.ProfileRoute
import kotlinx.serialization.Serializable

@Serializable
object Profile

fun NavGraphBuilder.profileScreen(
    onError: (BaseError) -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    navController: NavController
) {
    composable<Profile> {
        val viewModel: ProfileViewModel =
            it.sharedViewModel<ProfileViewModelImpl>(navController = navController)

        ProfileRoute(
            onError = onError,
            onSettingsButtonClicked = onSettingsButtonClicked,
            onPhotoClicked = onPhotoClicked,
            viewModel = viewModel
        )
    }
}
