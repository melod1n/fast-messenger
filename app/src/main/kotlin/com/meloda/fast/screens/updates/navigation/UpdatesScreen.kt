package com.meloda.fast.screens.updates.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.updates.presentation.UpdatesScreenContent

object UpdatesScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        UpdatesScreenContent(onBackClick = navigator::pop)
    }
}
