package com.meloda.fast.screens.photos.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.model.base.UiImage

@Immutable
data class PhotoViewArguments(
    val images: List<UiImage>
)
