package dev.meloda.fast.auth.login.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.meloda.fast.auth.login.LoginViewModel
import dev.meloda.fast.auth.login.model.LoginIntent
import dev.meloda.fast.auth.login.model.LoginEffect
import dev.meloda.fast.auth.login.model.LoginNavigationIntent
import dev.meloda.fast.auth.login.model.QrLoginState
import dev.meloda.fast.auth.login.presentation.LoginRoute
import dev.meloda.fast.auth.login.presentation.QrScannerScreen
import dev.meloda.fast.ui.extensions.sharedViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
object QrScannerRoute

fun NavGraphBuilder.loginScreen(
    handleNavigationIntent: (LoginNavigationIntent) -> Unit,
    navController: NavController
) {
    composable<Login> { backStackEntry ->
        val viewModel: LoginViewModel =
            backStackEntry.sharedViewModel<LoginViewModel>(navController = navController)

        val screenState by viewModel.screenStateFlow.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.screenEffectFlow.onEach { effect ->
                when (effect) {
                    LoginEffect.ClearValidationCode -> {
                        backStackEntry.savedStateHandle["validation_code"] = null
                    }

                    is LoginEffect.Navigate -> handleNavigationIntent(effect.intent)
                }
            }.collect()
        }

        LaunchedEffect(true) {
            val validationCode: String? = backStackEntry.savedStateHandle["validation_code"]
            viewModel.onValidationCodeReceived(validationCode)
        }

        LoginRoute(
            handleIntent = viewModel::handleIntent,
            screenState = screenState
        )
    }
}

fun NavGraphBuilder.qrScannerScreen(
    onAuthorized: () -> Unit,
    navController: NavController,
    onBackClicked: () -> Unit
) {
    composable<QrScannerRoute> { backStackEntry ->
        val viewModel: LoginViewModel = backStackEntry.sharedViewModel(navController = navController)
        val qrLoginState by viewModel.qrLoginStateFlow.collectAsStateWithLifecycle()

        DisposableEffect(viewModel) {
            viewModel.onQrScannerOpened()
            onDispose(viewModel::onQrScannerClosed)
        }

        LaunchedEffect(qrLoginState) {
            if (qrLoginState == QrLoginState.Authorized) onAuthorized()
        }

        QrScannerScreen(
            loginState = qrLoginState,
            onQrCodeScanned = { viewModel.handleIntent(LoginIntent.QrCodeScanned(it)) },
            onRetry = viewModel::retryQrScan,
            onBackClicked = onBackClicked
        )
    }
}
