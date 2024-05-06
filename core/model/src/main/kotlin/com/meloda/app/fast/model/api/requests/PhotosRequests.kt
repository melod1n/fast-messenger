package com.meloda.app.fast.model.api.requests

data class PhotosSaveMessagePhotoRequest(
    val photo: String,
    val server: Int,
    val hash: String
) {
    val map
        get() = mapOf(
            "photo" to photo,
            "server" to server.toString(),
            "hash" to hash
        )
}
