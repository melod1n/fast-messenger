package com.meloda.fast.screens.photos.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.model.base.UiImage

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
