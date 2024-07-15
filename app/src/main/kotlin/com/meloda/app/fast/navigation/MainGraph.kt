package com.meloda.app.fast.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.conversations.navigation.Conversations
import com.meloda.app.fast.friends.navigation.Friends
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.model.BottomNavigationItem
import com.meloda.app.fast.presentation.MainScreen
import com.meloda.app.fast.profile.navigation.Profile
import kotlinx.serialization.Serializable
import com.meloda.app.fast.ui.R as UiR

@Serializable
object MainGraph

@Serializable
object Main

fun NavGraphBuilder.mainScreen(
    onError: (BaseError) -> Unit,
    onSettingsButtonClicked: () -> Unit,
    onConversationClicked: (conversationId: Int) -> Unit,
) {
    val navigationItems = listOf(
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
            route = Conversations
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
            onConversationItemClicked = onConversationClicked,
        )
    }
}
