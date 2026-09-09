package dev.meloda.fast.profile.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.meloda.fast.profile.ProfileViewModel
import dev.meloda.fast.profile.presentation.ProfileRoute
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class Profile(val userId: Long? = null) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle): Profile =
            savedStateHandle.toRoute<Profile>()
    }
}

fun NavGraphBuilder.profileScreen(
    onBack: (() -> Unit)? = null,
    onSendMessageClicked: (userId: Long) -> Unit = {},
    onSettingsButtonClicked: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {}
) {
    composable<Profile> {
        val viewModel: ProfileViewModel = koinViewModel()
        val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()

        ProfileRoute(
            screenState = screenState,
            onBack = onBack,
            onSendMessageClicked = onSendMessageClicked,
            onSettingsButtonClicked = onSettingsButtonClicked,
            onPhotoClicked = onPhotoClicked,
            onRefresh = viewModel::onRefresh
        )
    }
}

fun NavController.navigateToProfile(userId: Long? = null) {
    this.navigate(Profile(userId))
}
