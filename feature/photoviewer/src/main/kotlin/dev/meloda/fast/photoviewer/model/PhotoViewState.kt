package dev.meloda.fast.photoviewer.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.model.UiImage

@Immutable
data class PhotoViewState(
    val images: List<UiImage>
) {

    companion object {
        val EMPTY: PhotoViewState = PhotoViewState(
            images = emptyList()
        )
    }
}
