package com.meloda.app.fast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.meloda.app.fast.auth.navigation.AuthGraph
import com.meloda.app.fast.auth.navigation.authNavGraph
import com.meloda.app.fast.auth.navigation.navigateToAuth
import com.meloda.app.fast.conversations.presentation.Conversations
import com.meloda.app.fast.conversations.presentation.conversationsScreen
import com.meloda.app.fast.datastore.UserConfig
import com.meloda.app.fast.settings.presentation.navigateToSettings
import com.meloda.app.fast.settings.presentation.settingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootNavigation(navController: NavHostController = rememberNavController()) {
    val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.accountsLoaded) {
        val isNeedToShowConversations by remember {
            derivedStateOf { screenState.accounts.isNotEmpty() && UserConfig.isLoggedIn() }
        }

        NavHost(
            navController = navController,
            startDestination = if (isNeedToShowConversations) Conversations else AuthGraph
        ) {
            authNavGraph(
                onError = { error -> },
                navController = navController
            )
            conversationsScreen(
                onError = { error -> },
                onNavigateToSettings = navController::navigateToSettings,
                onNavigateToMessagesHistory = { id -> }
            )
            settingsScreen(
                onBack = navController::navigateUp,
                onNavigateToAuth = navController::navigateToAuth,
                onNavigateToLanguagePicker = {

                }
            )
        }
    }

    NotificationsPermissionChecker(
        screenState = screenState,
        viewModel = viewModel
    )
}
