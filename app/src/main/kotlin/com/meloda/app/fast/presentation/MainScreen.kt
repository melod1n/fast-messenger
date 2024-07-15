package com.meloda.app.fast.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.meloda.app.fast.conversations.navigation.conversationsScreen
import com.meloda.app.fast.ui.theme.LocalBottomPadding
import com.meloda.app.fast.ui.theme.LocalHazeState
import com.meloda.app.fast.ui.theme.LocalTheme
import com.meloda.app.fast.friends.navigation.friendsScreen
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.model.BottomNavigationItem
import com.meloda.app.fast.navigation.MainGraph
import com.meloda.app.fast.profile.navigation.profileScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MainScreen(
    navigationItems: List<BottomNavigationItem>,
    onError: (BaseError) -> Unit = {},
    onSettingsButtonClicked: () -> Unit = {},
    onConversationItemClicked: (conversationId: Int) -> Unit = {}
) {
    val currentTheme = LocalTheme.current
    val hazeState = remember { HazeState() }
    val navController = rememberNavController()

    var selectedItemIndex by rememberSaveable {
        mutableIntStateOf(1)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .then(
                        if (currentTheme.usingBlur) {
                            Modifier.hazeChild(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else Modifier
                    )
                    .fillMaxWidth(),
                containerColor = NavigationBarDefaults.containerColor.copy(
                    alpha = if (currentTheme.usingBlur) 0f else 1f
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (currentTheme.usingBlur) 0.dp else padding.calculateBottomPadding())
        ) {
            CompositionLocalProvider(
                LocalHazeState provides hazeState,
                LocalBottomPadding provides if (currentTheme.usingBlur) padding.calculateBottomPadding() else 0.dp
            ) {
                NavHost(
                    navController = navController,
                    startDestination = MainGraph,
                    enterTransition = { fadeIn(animationSpec = tween(200)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) {
                    navigation<MainGraph>(startDestination = navigationItems[selectedItemIndex].route) {
                        friendsScreen(
                            onError = onError,
                            navController = navController
                        )
                        conversationsScreen(
                            onError = onError,
                            onConversationItemClicked = onConversationItemClicked,
                            navController = navController
                        )
                        profileScreen(
                            onError = onError,
                            onSettingsButtonClicked = onSettingsButtonClicked,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
