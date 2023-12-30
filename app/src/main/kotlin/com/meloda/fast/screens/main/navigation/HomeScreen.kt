package com.meloda.fast.screens.main.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.androidx.AndroidScreen
import com.meloda.fast.screens.main.MainViewModel
import com.meloda.fast.screens.main.presentation.HomeScreenContent

data class HomeScreen(private val viewModel: MainViewModel) : AndroidScreen() {

    @Composable
    override fun Content() = HomeScreenContent(viewModel)
}
