package com.meloda.app.fast.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.meloda.app.fast.MainViewModel
import com.meloda.app.fast.MainViewModelImpl
import com.meloda.app.fast.auth.AuthGraph
import com.meloda.app.fast.auth.authNavGraph
import com.meloda.app.fast.auth.navigateToAuth
import com.meloda.app.fast.chatmaterials.navigation.chatMaterialsScreen
import com.meloda.app.fast.chatmaterials.navigation.navigateToChatMaterials
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.languagepicker.navigation.languagePickerScreen
import com.meloda.app.fast.languagepicker.navigation.navigateToLanguagePicker
import com.meloda.app.fast.messageshistory.navigation.messagesHistoryScreen
import com.meloda.app.fast.messageshistory.navigation.navigateToMessagesHistory
import com.meloda.app.fast.navigation.Main
import com.meloda.app.fast.navigation.mainScreen
import com.meloda.app.fast.settings.navigation.navigateToSettings
import com.meloda.app.fast.settings.navigation.settingsScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootScreen(navController: NavHostController = rememberNavController()) {
    val viewModel: MainViewModel = koinViewModel<MainViewModelImpl>()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isNeedToOpenAuth by viewModel.isNeedToOpenAuth.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenAuth) {
        if (isNeedToOpenAuth) {
            viewModel.onNavigatedToAuth()
            navController.navigateToAuth(clearBackStack = true)
        }
    }

    if (screenState.accountsLoaded) {
        val isNeedToShowConversations = remember {
            screenState.accounts.isNotEmpty() && UserConfig.isLoggedIn()
        }

        NavHost(
            navController = navController,
            startDestination = if (isNeedToShowConversations) Main else AuthGraph,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            authNavGraph(
                onNavigateToMain = navController::navigateToMain,
                navController = navController
            )
            mainScreen(
                onError = viewModel::onError,
                onSettingsButtonClicked = navController::navigateToSettings,
                onConversationClicked = navController::navigateToMessagesHistory
            )

            messagesHistoryScreen(
                onError = viewModel::onError,
                onBack = navController::navigateUp,
                onChatMaterialsDropdownItemClicked = navController::navigateToChatMaterials
            )
            chatMaterialsScreen(
                onBack = navController::navigateUp
            )

            settingsScreen(
                onBack = navController::navigateUp,
                onLogOutButtonClicked = { navController.navigateToAuth(true) },
                onLanguageItemClicked = navController::navigateToLanguagePicker
            )
            languagePickerScreen(onBack = navController::navigateUp)
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
