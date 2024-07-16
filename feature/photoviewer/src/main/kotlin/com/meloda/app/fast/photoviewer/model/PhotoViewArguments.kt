package dev.meloda.fast.photoviewer.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.model.UiImage

@Immutable
data class PhotoViewArguments(
    val images: List<UiImage>
)
