package com.meloda.app.fast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.meloda.app.fast.conversations.navigation.Conversations
import com.meloda.app.fast.conversations.navigation.conversationsRoute
import com.meloda.app.fast.friends.navigation.Friends
import com.meloda.app.fast.friends.navigation.friendsRoute
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable
import com.meloda.app.fast.designsystem.R as UiR

@Serializable
object MainGraph

@Serializable
object Main

@Serializable
object Profile

data class BottomNavigationItem(
    val titleResId: Int,
    val selectedIconResId: Int,
    val unselectedIconResId: Int,
    val route: Any,
)

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.mainScreen(
    onError: (BaseError) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMessagesHistory: (conversationId: Int) -> Unit,
) {
    val items = listOf(
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
    val routes = items.map(BottomNavigationItem::route)

    composable<Main> {
        val navController = rememberNavController()

        var selectedItemIndex by rememberSaveable {
            mutableIntStateOf(1)
        }

        var isBottomBarVisible by rememberSaveable {
            mutableStateOf(true)
        }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = isBottomBarVisible,
                    enter = slideIn { IntOffset(0, 400) },
                    exit = slideOut { IntOffset(0, 400) }
                ) {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedItemIndex == index,
                                onClick = {
                                    if (selectedItemIndex != index) {
                                        val currentRoute = routes[selectedItemIndex]

                                        selectedItemIndex = index
                                        navController.navigate(item.route) {
                                            popUpTo(route = currentRoute) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (selectedItemIndex == index) item.selectedIconResId
                                            else item.unselectedIconResId
                                        ),
                                        contentDescription = null
                                    )
                                },
                                alwaysShowLabel = false
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
            ) {
                NavHost(
                    navController = navController,
                    startDestination = MainGraph,
                    enterTransition = { fadeIn(animationSpec = tween(350)) },
                    exitTransition = { fadeOut(animationSpec = tween(350)) }
                ) {
                    navigation<MainGraph>(startDestination = Conversations) {
                        friendsRoute(
                            onError = onError,
                            navController = navController
                        )
                        conversationsRoute(
                            onError = onError,
                            onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                            navController = navController,
                            onListScrollingUp = { isScrolling ->
//                                isBottomBarVisible = isScrolling
                            }
                        )

                        composable<Profile> {
                            Scaffold(
                                topBar = {
                                    TopAppBar(
                                        title = {
                                            Text(text = stringResource(id = UiR.string.title_profile))
                                        },
                                        actions = {
                                            IconButton(onClick = onNavigateToSettings) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Settings,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                }
                            ) { padding ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding)
                                ) {

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
