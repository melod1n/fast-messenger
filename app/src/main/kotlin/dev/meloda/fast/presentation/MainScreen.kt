package dev.meloda.fast.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.conversations.navigation.ConversationsGraph
import dev.meloda.fast.conversations.navigation.conversationsGraph
import dev.meloda.fast.friends.navigation.Friends
import dev.meloda.fast.friends.navigation.friendsScreen
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.BottomNavigationItem
import dev.meloda.fast.navigation.MainGraph
import dev.meloda.fast.profile.navigation.profileScreen
import dev.meloda.fast.ui.theme.LocalBottomPadding
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalNavController
import dev.meloda.fast.ui.theme.LocalReselectedTab
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.theme.LocalUser
import dev.meloda.fast.ui.util.ImmutableList

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MainScreen(
    navigationItems: ImmutableList<BottomNavigationItem>,
    onError: (BaseError) -> Unit = {},
    onSettingsButtonClicked: () -> Unit = {},
    onNavigateToMessagesHistory: (conversationId: Long) -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {},
    onMessageClicked: (userid: Long) -> Unit = {},
    onNavigateToCreateChat: () -> Unit = {}
) {
    val activity = LocalActivity.current as? AppCompatActivity ?: return
    val theme = LocalThemeConfig.current
    val hazeState = remember { HazeState(true) }
    val navController = rememberNavController()

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(1)
    }

    BackHandler(enabled = selectedItemIndex != 1) {
        val currentRoute = navigationItems[selectedItemIndex].route

        selectedItemIndex = 1
        navController.navigate(navigationItems[selectedItemIndex].route) {
            popUpTo(route = currentRoute) {
                inclusive = true
            }
        }
    }

    val profileImageUrl = LocalUser.current?.photo100

    var tabReselected by remember {
        mutableStateOf(navigationItems.associate { it.route to false })
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (theme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.regular(NavigationBarDefaults.containerColor)
                            )
                        } else Modifier
                    ),
                containerColor = if (theme.enableBlur) Color.Transparent
                else NavigationBarDefaults.containerColor
            ) {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            if (selectedItemIndex != index) {
                                val currentRoute = navigationItems[selectedItemIndex].route

                                selectedItemIndex = index
                                navController.navigate(item.route) {
                                    popUpTo(route = currentRoute) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                tabReselected = tabReselected.toMutableMap().also {
                                    it[navigationItems[index].route] = true
                                }
                            }
                        },
                        icon = {
                            if (index == navigationItems.size - 1) {
                                var isLoading by remember {
                                    mutableStateOf(true)
                                }
                                if (isLoading) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (selectedItemIndex == index) item.selectedIconResId
                                            else item.unselectedIconResId
                                        ),
                                        contentDescription = null
                                    )
                                }
                                SubcomposeAsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = "Profile Image",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .alpha(if (isLoading) 0f else 1f),
                                    onSuccess = { isLoading = false }
                                )
                            } else {
                                Icon(
                                    painter = painterResource(
                                        id = if (selectedItemIndex == index) item.selectedIconResId
                                        else item.unselectedIconResId
                                    ),
                                    contentDescription = null
                                )
                            }
                        },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CompositionLocalProvider(
                LocalHazeState provides hazeState,
                LocalBottomPadding provides padding.calculateBottomPadding(),
                LocalReselectedTab provides tabReselected,
                LocalNavController provides navController
            ) {
                NavHost(
                    navController = navController,
                    startDestination = MainGraph,
                    enterTransition = { fadeIn(animationSpec = tween(200)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) {
                    navigation<MainGraph>(
                        startDestination = navigationItems[selectedItemIndex].route,
                        enterTransition = { fadeIn(animationSpec = tween(200)) },
                        exitTransition = { fadeOut(animationSpec = tween(200)) }
                    ) {
                        friendsScreen(
                            activity = activity,
                            onError = onError,
                            onPhotoClicked = onPhotoClicked,
                            onMessageClicked = onMessageClicked,
                            onScrolledToTop = {
                                tabReselected = tabReselected.toMutableMap().also {
                                    it[Friends] = false
                                }
                            },
                        )
                        conversationsGraph(
                            activity = activity,
                            onError = onError,
                            onNavigateToMessagesHistory = onNavigateToMessagesHistory,
                            onNavigateToCreateChat = onNavigateToCreateChat,
                            onScrolledToTop = {
                                tabReselected = tabReselected.toMutableMap().also {
                                    it[ConversationsGraph] = false
                                }
                            }
                        )
                        profileScreen(
                            activity = activity,
                            onError = onError,
                            onSettingsButtonClicked = onSettingsButtonClicked,
                            onPhotoClicked = onPhotoClicked
                        )
                    }
                }
            }
        }
    }
}
