package com.meloda.fast.screens.main.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.meloda.fast.screens.main.MainViewModel
import com.meloda.fast.screens.main.presentation.HomeScreenContent

data class HomeScreen(private val viewModel: MainViewModel) : Screen {

    @Composable
    override fun Content() = HomeScreenContent(viewModel)
}
