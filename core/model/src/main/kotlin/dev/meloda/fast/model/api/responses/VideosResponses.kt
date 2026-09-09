package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.VkVideoData

@JsonClass(generateAdapter = true)
data class VideosSaveResponse(
    @Json(name = "access_key") val accessKey: String,
    val description: String,
    @Json(name = "owner_id") val ownerid: Long,
    val title: String,
    @Json(name = "upload_url") val uploadUrl: String,
    @Json(name = "video_id") val videoid: Long
)

@JsonClass(generateAdapter = true)
data class VideosUploadResponse(
    @Json(name = "video_hash") val hash: String? = null,
    val size: Int? = null,
    @Json(name = "direct_link") val directLink: String? = null,
    @Json(name = "owner_id") val ownerid: Long? = null,
    @Json(name = "video_id") val videoid: Long? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class VideosGetResponse(
    val count: Int,
    val items: List<VkVideoData>
)
