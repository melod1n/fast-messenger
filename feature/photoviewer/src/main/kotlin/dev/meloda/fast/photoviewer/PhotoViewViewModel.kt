package dev.meloda.fast.photoviewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.photoviewer.model.PhotoViewScreenState
import dev.meloda.fast.photoviewer.navigation.PhotoView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.URLDecoder

interface PhotoViewViewModel {
    val screenState: StateFlow<PhotoViewScreenState>
}

class PhotoViewViewModelImpl(
    savedStateHandle: SavedStateHandle
) : PhotoViewViewModel, ViewModel() {

    override val screenState = MutableStateFlow(PhotoViewScreenState.EMPTY)

    init {
        val arguments = PhotoView.from(savedStateHandle).arguments

        screenState.setValue { old ->
            old.copy(
                images = arguments.images
                    .map { URLDecoder.decode(it, "utf-8") }
                    .map(UiImage::Url)
            )
        }
    }
}
