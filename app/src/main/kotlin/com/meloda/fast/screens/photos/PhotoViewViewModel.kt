package com.meloda.fast.screens.photos

import androidx.lifecycle.ViewModel
import com.meloda.fast.ext.setValue
import com.meloda.fast.screens.photos.model.PhotoViewArguments
import com.meloda.fast.screens.photos.model.PhotoViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface PhotoViewViewModel {
    val state: StateFlow<PhotoViewState>

    fun setArguments(arguments: PhotoViewArguments)
}

class PhotoViewViewModelImpl : PhotoViewViewModel, ViewModel() {
    override val state = MutableStateFlow(PhotoViewState.EMPTY)

    override fun setArguments(arguments: PhotoViewArguments) {
        state.setValue { old -> old.copy(images = arguments.images) }
    }
}
