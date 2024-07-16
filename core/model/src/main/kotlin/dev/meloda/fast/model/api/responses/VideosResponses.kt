package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json

data class VideosSaveResponse(
    @Json(name = "access_key") val accessKey: String,
    val description: String,
    @Json(name = "owner_id") val ownerId: Int,
    val title: String,
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "video_id") val videoId: Int
)

data class VideosUploadResponse(
    @Json(name = "video_hash") val hash: String?,
    val size: Int,
    @Json(name = "direct_link") val directLink: String,
    @Json(name = "owner_id") val ownerId: Int,
    @Json(name = "video_id") val videoId: Int,
    val error: String?
)
