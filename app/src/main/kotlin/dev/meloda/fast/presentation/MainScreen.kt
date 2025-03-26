package dev.meloda.fast.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.MainViewModel
import dev.meloda.fast.conversations.navigation.conversationsScreen
import dev.meloda.fast.friends.navigation.friendsScreen
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.BottomNavigationItem
import dev.meloda.fast.navigation.MainGraph
import dev.meloda.fast.profile.navigation.profileScreen
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MainScreen(
    navigationItems: ImmutableList<BottomNavigationItem>,
    onError: (BaseError) -> Unit = {},
    onSettingsButtonClicked: () -> Unit = {},
    onConversationItemClicked: (conversationId: Int) -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {},
    onMessageClicked: (userId: Int) -> Unit = {},
    onCreateChatClicked: () -> Unit = {},
    viewModel: MainViewModel
) {
    val currentTheme = LocalThemeConfig.current
    val hazeState = remember { HazeState() }
    val navController = rememberNavController()

    val profileImageUrl by viewModel.profileImageUrl.collectAsStateWithLifecycle()

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(1)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .then(
                        if (currentTheme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else Modifier
                    )
                    .fillMaxWidth(),
                containerColor = NavigationBarDefaults.containerColor.copy(
                    alpha = if (currentTheme.enableBlur) 0f else 1f
                )
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
                                    onSuccess = {
                                        isLoading = false
                                    }
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
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            CompositionLocalProvider(
                LocalHazeState provides hazeState,
//                LocalBottomPadding provides if (currentTheme.enableBlur) padding.calculateBottomPadding() else 0.dp
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
                            onError = onError,
                            onPhotoClicked = onPhotoClicked,
                            onMessageClicked = onMessageClicked
                        )
                        conversationsScreen(
                            onError = onError,
                            onConversationItemClicked = onConversationItemClicked,
                            onCreateChatClicked = onCreateChatClicked,
                            navController = navController,
                        )
                        profileScreen(
                            onError = onError,
                            onSettingsButtonClicked = onSettingsButtonClicked,
                            onPhotoClicked = onPhotoClicked,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
