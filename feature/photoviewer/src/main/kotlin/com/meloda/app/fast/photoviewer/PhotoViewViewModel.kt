package com.meloda.app.fast.photoviewer

import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.photoviewer.model.PhotoViewArguments
import com.meloda.app.fast.photoviewer.model.PhotoViewState
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
