package dev.meloda.fast.photoviewer.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.model.UiImage

@Immutable
data class PhotoViewScreenState(
    val images: List<UiImage>,
    val selectedPage: Int
) {

    companion object {
        val EMPTY: PhotoViewScreenState = PhotoViewScreenState(
            images = emptyList(),
            selectedPage = 0
        )
    }
}
