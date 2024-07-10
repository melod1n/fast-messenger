package com.meloda.app.fast.model.api.responses

import com.squareup.moshi.Json

data class PhotosGetMessagesUploadServerResponse(
    @Json(name = "album_id")
    val albumId: Int,
    @Json(name = "upload_url")
    val uploadUrl: String
)

data class PhotosUploadPhotoResponse(
    val server: Int,
    val photo: String,
    val hash: String
)
