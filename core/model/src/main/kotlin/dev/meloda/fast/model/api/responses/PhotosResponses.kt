package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json

data class PhotosGetMessagesUploadServerResponse(
    @Json(name = "album_id")
    val albumid: Long,
    @Json(name = "upload_url")
    val uploadUrl: String
)

data class PhotosUploadPhotoResponse(
    val server: Int,
    val photo: String,
    val hash: String
)
