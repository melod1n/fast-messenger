package com.meloda.fast.screens.photos.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.meloda.fast.screens.photos.PhotoViewViewModel
import com.meloda.fast.screens.photos.PhotoViewViewModelImpl
import com.meloda.fast.screens.photos.model.PhotoViewArguments
import com.meloda.fast.screens.photos.presentation.PhotoViewScreenContent
import org.koin.androidx.compose.koinViewModel

data class PhotoViewScreen(
    val arguments: PhotoViewArguments
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: PhotoViewViewModel = koinViewModel<PhotoViewViewModelImpl>()

        PhotoViewScreenContent(
            onBackClick = navigator::pop,
            images = arguments.images,
            viewModel = viewModel
        )
    }
}
