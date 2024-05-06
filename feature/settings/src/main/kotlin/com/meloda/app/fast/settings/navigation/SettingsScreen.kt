package com.meloda.app.fast.settings.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.app.fast.settings.SettingsViewModel
import com.meloda.app.fast.settings.SettingsViewModelImpl
import com.meloda.app.fast.settings.presentation.SettingsScreenContent
import org.koin.androidx.compose.koinViewModel

data class SettingsScreen(
    private val restart: () -> Unit
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()

        SettingsScreenContent(
            onBackClick = navigator::pop,
            navigateToLanguagePicker = {
                // TODO: 05/05/2024, Danil Nikolaev: implement
            },
            navigateToLogin = {
                // TODO: 05/05/2024, Danil Nikolaev: implement
            },
            viewModel = viewModel
        )
    }
}
