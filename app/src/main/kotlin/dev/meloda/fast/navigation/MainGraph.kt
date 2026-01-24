package dev.meloda.fast.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.convos.navigation.ConvoGraph
import dev.meloda.fast.friends.navigation.Friends
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.BottomNavigationItem
import dev.meloda.fast.presentation.MainScreen
import dev.meloda.fast.profile.navigation.Profile
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.serialization.Serializable
import dev.meloda.fast.ui.R

@Serializable
object MainGraph

@Serializable
object Main

fun NavGraphBuilder.mainScreen(
    onError: (BaseError) -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onNavigateToMessagesHistory: (convoId: Long) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userid: Long) -> Unit,
    onNavigateToCreateChat: () -> Unit
) {
    val navigationItems = ImmutableList.of(
        BottomNavigationItem(
            titleResId = R.string.title_friends,
            selectedIconResId = R.drawable.ic_group_fill_round_24,
            unselectedIconResId = R.drawable.ic_group_round_24,
            route = Friends,
        ),
        BottomNavigationItem(
            titleResId = R.string.title_convos,
            selectedIconResId = R.drawable.ic_mail_fill_round_24,
            unselectedIconResId = R.drawable.ic_mail_round_24,
            route = ConvoGraph
        ),
        BottomNavigationItem(
            titleResId = R.string.title_profile,
            selectedIconResId = R.drawable.ic_account_circle_fill_round_24,
            unselectedIconResId = R.drawable.ic_account_circle_round_24,
            route = Profile
        )
    )

    composable<Main> {
        MainScreen(
            navigationItems = navigationItems,
            onError = onError,
            onSettingsButtonClicked = onSettingsButtonClicked,
            onNavigateToMessagesHistory = onNavigateToMessagesHistory,
            onPhotoClicked = onPhotoClicked,
            onMessageClicked = onMessageClicked,
            onNavigateToCreateChat = onNavigateToCreateChat
        )
    }
}
