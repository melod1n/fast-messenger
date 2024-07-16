package dev.meloda.fast.photoviewer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PhotoViewArguments(
    val images: List<String>
) : Parcelable
