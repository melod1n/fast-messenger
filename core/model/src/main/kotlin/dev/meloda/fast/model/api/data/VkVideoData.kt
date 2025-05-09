package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkVideoDomain

@JsonClass(generateAdapter = true)
data class VkVideoData(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String,
    @Json(name = "width") val width: Int?,
    @Json(name = "height") val height: Int?,
    @Json(name = "duration") val duration: Int,
    @Json(name = "date") val date: Int,
    @Json(name = "comments") val comments: Int?,
    @Json(name = "description") val description: String?,
    @Json(name = "player") val player: String?,
    @Json(name = "added") val added: Int?,
    @Json(name = "type") val type: String,
    @Json(name = "views") val views: Int,
    @Json(name = "access_key") val accessKey: String?,
    @Json(name = "owner_id") val ownerId: Long,
    @Json(name = "is_favorite") val isFavorite: Boolean?,
    @Json(name = "image") val image: List<Image>?,
    @Json(name = "first_frame") val firstFrame: List<FirstFrame>?,
    @Json(name = "files") val files: File?,
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Image(
        @Json(name = "width") val width: Int,
        @Json(name = "height") val height: Int,
        @Json(name = "url") val url: String,
        @Json(name = "with_padding") val withPadding: Int?
    ) {

        fun asVideoImage() = VkVideoDomain.VideoImage(
            width = width,
            height = height,
            url = url,
            withPadding = withPadding == 1
        )
    }

    @JsonClass(generateAdapter = true)
    data class FirstFrame(
        @Json(name = "height") val height: Int,
        @Json(name = "width") val width: Int,
        @Json(name = "url") val url: String
    )

    @JsonClass(generateAdapter = true)
    data class File(
        @Json(name = "mp4_240") val mp4240: String?,
        @Json(name = "mp4_360") val mp4360: String?,
        @Json(name = "mp4_480") val mp4480: String?,
        @Json(name = "mp4_720") val mp4720: String?,
        @Json(name = "mp4_1080") val mp41080: String?,
        @Json(name = "mp4_1440") val mp41440: String?,
        @Json(name = "hls") val hls: String?,
        @Json(name = "dash_uni") val dashUni: String?,
        @Json(name = "dash_sep") val dashSep: String?,
        @Json(name = "hls_ondemand") val hlsOnDemand: String?,
        @Json(name = "dash_ondemand") val dashOnDemand: String?,
        @Json(name = "failover_host") val failOverHost: String?
    )

    fun toDomain() = VkVideoDomain(
        id = id,
        ownerId = ownerId,
        images = image.orEmpty().map { it.asVideoImage() },
        firstFrames = firstFrame,
        accessKey = accessKey,
        title = title,
        views = views,
        duration = duration,
        isShortVideo = type == "short_video"
    )
}
