package dev.meloda.fast.photoviewer

import androidx.lifecycle.ViewModel
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.photoviewer.model.PhotoViewArguments
import dev.meloda.fast.photoviewer.model.PhotoViewState
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
