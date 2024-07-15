package com.meloda.app.fast.photoviewer.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.common.model.UiImage

@Immutable
data class PhotoViewArguments(
    val images: List<UiImage>
)
