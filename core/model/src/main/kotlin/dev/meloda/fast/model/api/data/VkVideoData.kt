package dev.meloda.fast.model.api.data

import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkVideoDomain

@JsonClass(generateAdapter = true)
data class VkVideoData(
    val id: Int,
    val title: String,
    val width: Int?,
    val height: Int?,
    val duration: Int,
    val date: Int,
    val comments: Int?,
    val description: String?,
    val player: String?,
    val added: Int?,
    val type: String,
    val views: Int,
    val access_key: String?,
    val owner_id: Int,
    val is_favorite: Boolean?,
    val image: List<Image>?,
    val first_frame: List<FirstFrame>?,
    val files: File?
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Image(
        val width: Int,
        val height: Int,
        val url: String,
        val with_padding: Int?
    ) {

        fun asVideoImage() = VkVideoDomain.VideoImage(
            width = width,
            height = height,
            url = url,
            withPadding = with_padding == 1
        )
    }

    @JsonClass(generateAdapter = true)
    data class FirstFrame(
        val height: Int,
        val width: Int,
        val url: String
    )

    @JsonClass(generateAdapter = true)
    data class File(
        val mp4_240: String?,
        val mp4_360: String?,
        val mp4_480: String?,
        val mp4_720: String?,
        val mp4_1080: String?,
        val mp4_1440: String?,
        val hls: String?,
        val dash_uni: String?,
        val dash_sep: String?,
        val hls_ondemand: String?,
        val dash_ondemand: String?,
        val failover_host: String?
    )

    fun toDomain() = VkVideoDomain(
        id = id,
        ownerId = owner_id,
        images = image.orEmpty().map { it.asVideoImage() },
        firstFrames = first_frame,
        accessKey = access_key,
        title = title
    )
}
