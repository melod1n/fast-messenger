package com.meloda.app.fast.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.common.extensions.navigation.sharedViewModel
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.profile.ProfileViewModel
import com.meloda.app.fast.profile.ProfileViewModelImpl
import com.meloda.app.fast.profile.presentation.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable
object Profile

fun NavGraphBuilder.profileRoute(
    onError: (BaseError) -> Unit,
    onNavigateToSettings: () -> Unit,
    navController: NavController
) {
    composable<Profile> {
        val viewModel: ProfileViewModel =
            it.sharedViewModel<ProfileViewModelImpl>(navController = navController)

        ProfileScreen(
            onError = onError,
            onNavigateToSettings = onNavigateToSettings,
            viewModel = viewModel
        )
    }
}
