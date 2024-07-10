package com.meloda.app.fast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.meloda.app.fast.auth.AuthGraph
import com.meloda.app.fast.auth.authNavGraph
import com.meloda.app.fast.auth.navigateToAuth
import com.meloda.app.fast.chatmaterials.navigation.chatMaterialsRoute
import com.meloda.app.fast.chatmaterials.navigation.navigateToChatMaterials
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.languagepicker.navigation.languagePickerRoute
import com.meloda.app.fast.languagepicker.navigation.navigateToLanguagePicker
import com.meloda.app.fast.messageshistory.navigation.messagesHistoryRoute
import com.meloda.app.fast.messageshistory.navigation.navigateToMessagesHistory
import com.meloda.app.fast.settings.presentation.navigateToSettings
import com.meloda.app.fast.settings.presentation.settingsRoute
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootGraph(navController: NavHostController = rememberNavController()) {
    val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.isNeedToOpenAuth) {
        viewModel.onAuthOpened()
        navController.navigateToAuth(clearBackStack = true)
    }

    if (screenState.accountsLoaded) {
        val isNeedToShowConversations by remember {
            derivedStateOf { screenState.accounts.isNotEmpty() && UserConfig.isLoggedIn() }
        }

        NavHost(
            navController = navController,
            startDestination = if (isNeedToShowConversations) Main else AuthGraph
        ) {
            authNavGraph(
                onError = viewModel::onError,
                onNavigateToMain = navController::navigateToMain,
                navController = navController
            )
            mainScreen(
                onError = viewModel::onError,
                onNavigateToSettings = navController::navigateToSettings,
                onNavigateToMessagesHistory = navController::navigateToMessagesHistory
            )

            messagesHistoryRoute(
                onError = viewModel::onError,
                onBack = navController::navigateUp,
                onNavigateToChatAttachments = navController::navigateToChatMaterials
            )
            chatMaterialsRoute(
                onBack = navController::navigateUp
            )

            settingsRoute(
                onError = viewModel::onError,
                onBack = navController::navigateUp,
                onNavigateToAuth = { navController.navigateToAuth(true) },
                onNavigateToLanguagePicker = navController::navigateToLanguagePicker
            )
            languagePickerRoute(onBack = navController::navigateUp)
        }
    }

    NotificationsPermissionChecker(
        screenState = screenState,
        viewModel = viewModel
    )
}

fun NavController.navigateToMain() {
    this.navigate(Main) {
        popUpTo(0) {
            inclusive = true
        }
    }
}
