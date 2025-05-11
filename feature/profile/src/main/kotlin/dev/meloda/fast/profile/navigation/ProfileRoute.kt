package dev.meloda.fast.profile.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.profile.ProfileViewModel
import dev.meloda.fast.profile.ProfileViewModelImpl
import dev.meloda.fast.profile.presentation.ProfileRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.ext.android.getViewModel

@Serializable
object Profile

fun NavGraphBuilder.profileScreen(
    activity: AppCompatActivity,
    onError: (BaseError) -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onPhotoClicked: (url: String) -> Unit
) {
    val viewModel: ProfileViewModel = with(activity) {
        getViewModel<ProfileViewModelImpl>()
    }
    composable<Profile> {
        ProfileRoute(
            onError = onError,
            onSettingsButtonClicked = onSettingsButtonClicked,
            onPhotoClicked = onPhotoClicked,
            viewModel = viewModel
        )
    }
}
