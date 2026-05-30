package dev.meloda.fast.profile.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.profile.ProfileViewModel
import dev.meloda.fast.profile.presentation.ProfileRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.ext.android.getViewModel

@Serializable
object Profile

fun NavGraphBuilder.profileScreen(
    activity: AppCompatActivity,
    onSettingsButtonClicked: () -> Unit,
    onPhotoClicked: (url: String) -> Unit
) {
    val viewModel: ProfileViewModel = with(activity) { getViewModel() }

    composable<Profile> {
        val screenState by viewModel.screenStateFlow().collectAsStateWithLifecycle()

        ProfileRoute(
            screenState = screenState,
            onSettingsButtonClicked = onSettingsButtonClicked,
            onPhotoClicked = onPhotoClicked,
        )
    }
}
