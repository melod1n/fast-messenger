package dev.meloda.fast.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.conversations.navigation.ConversationsGraph
import dev.meloda.fast.friends.navigation.Friends
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.BottomNavigationItem
import dev.meloda.fast.presentation.MainScreen
import dev.meloda.fast.profile.navigation.Profile
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.serialization.Serializable
import dev.meloda.fast.ui.R as UiR

@Serializable
object MainGraph

@Serializable
object Main

fun NavGraphBuilder.mainScreen(
    onError: (BaseError) -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onNavigateToMessagesHistory: (conversationId: Long) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userid: Long) -> Unit,
    onNavigateToCreateChat: () -> Unit
) {
    val navigationItems = ImmutableList.of(
        BottomNavigationItem(
            titleResId = UiR.string.title_friends,
            selectedIconResId = UiR.drawable.baseline_people_alt_24,
            unselectedIconResId = UiR.drawable.outline_people_alt_24,
            route = Friends,
        ),
        BottomNavigationItem(
            titleResId = UiR.string.title_conversations,
            selectedIconResId = UiR.drawable.baseline_chat_24,
            unselectedIconResId = UiR.drawable.outline_chat_24,
            route = ConversationsGraph
        ),
        BottomNavigationItem(
            titleResId = UiR.string.title_profile,
            selectedIconResId = UiR.drawable.baseline_account_circle_24,
            unselectedIconResId = UiR.drawable.outline_account_circle_24,
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
