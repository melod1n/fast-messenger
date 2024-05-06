package com.meloda.app.fast.photoviewer.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.app.fast.photoviewer.PhotoViewViewModel
import com.meloda.app.fast.photoviewer.PhotoViewViewModelImpl
import com.meloda.app.fast.photoviewer.model.PhotoViewArguments
import com.meloda.app.fast.photoviewer.presentation.PhotoViewScreenContent
import org.koin.androidx.compose.koinViewModel

data class PhotoViewScreen(
    private val arguments: PhotoViewArguments
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: PhotoViewViewModel = koinViewModel<PhotoViewViewModelImpl>()
        viewModel.setArguments(arguments)

        PhotoViewScreenContent(
            onBackClick = navigator::pop,
            viewModel = viewModel
        )
    }
}
