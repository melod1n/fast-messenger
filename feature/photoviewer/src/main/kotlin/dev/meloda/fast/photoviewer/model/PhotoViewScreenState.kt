package dev.meloda.fast.photoviewer.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.ImmutableList
import dev.meloda.fast.common.emptyImmutableList
import dev.meloda.fast.common.model.UiImage

@Immutable
data class PhotoViewScreenState(
    val images: ImmutableList<UiImage>,
    val selectedPage: Int,
    val isLoading: Boolean
) {

    companion object {
        val EMPTY: PhotoViewScreenState = PhotoViewScreenState(
            images = emptyImmutableList(),
            selectedPage = 0,
            isLoading = false
        )
    }
}
