package com.meloda.app.fast.photoviewer.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.common.UiImage

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
