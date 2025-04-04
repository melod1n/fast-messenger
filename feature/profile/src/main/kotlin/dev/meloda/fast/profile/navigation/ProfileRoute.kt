package dev.meloda.fast.profile.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.profile.ProfileViewModel
import dev.meloda.fast.profile.ProfileViewModelImpl
import dev.meloda.fast.profile.presentation.ProfileRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
object Profile

fun NavGraphBuilder.profileScreen(
    onError: (BaseError) -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onPhotoClicked: (url: String) -> Unit
) {
    composable<Profile> {
        val context = LocalContext.current
        val viewModel: ProfileViewModel = koinViewModel<ProfileViewModelImpl>(
            viewModelStoreOwner = context as AppCompatActivity
        )

        ProfileRoute(
            onError = onError,
            onSettingsButtonClicked = onSettingsButtonClicked,
            onPhotoClicked = onPhotoClicked,
            viewModel = viewModel
        )
    }
}
