package com.meloda.fast.api.network.photos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotosSaveMessagePhotoRequest(
    val photo: String, val server: Int, val hash: String
) : Parcelable {
    val map
        get() = mapOf(
            "photo" to photo,
            "server" to server.toString(),
            "hash" to hash
        )
}