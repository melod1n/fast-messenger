package com.meloda.fast.modules.auth.screens.logo.navigation

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.modules.auth.screens.login.navigation.LoginScreen
import com.meloda.fast.modules.auth.screens.logo.LogoViewModel
import com.meloda.fast.modules.auth.screens.logo.LogoViewModelImpl
import com.meloda.fast.modules.auth.screens.logo.model.UiAction
import com.meloda.fast.modules.auth.screens.logo.presentation.LogoScreenContent
import com.meloda.fast.screens.main.MainActivity
import org.koin.androidx.compose.koinViewModel

object LogoScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel: LogoViewModel = koinViewModel<LogoViewModelImpl>()
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow

        LogoScreenContent(
            onAction = { action ->
                when (action) {
                    UiAction.NextClicked -> {
                        navigator.push(LoginScreen)
                    }

                    is UiAction.Restart -> {
                        (context as? Activity)?.let { activity ->
                            activity.finish()
                            activity.startActivity(Intent(activity, MainActivity::class.java))
                        }
                    }
                }
            },
            viewModel = viewModel
        )
    }
}
