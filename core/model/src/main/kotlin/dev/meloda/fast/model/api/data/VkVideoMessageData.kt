package dev.meloda.fast.model.api.data

import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkVideoMessageDomain

@JsonClass(generateAdapter = true)
data class VkVideoMessageData(
    val id: Long,
    val access_key: String?,
    val can_add: Int?,
    val can_dislike: Int?,
    val can_download: Int?,
    val can_play_in_background: Int?,
    val date: Int?,
    val description: String?,
    val duration: Int?,
    val files: Files?,
    val first_frame: List<FirstFrame>?,
    val height: Int?,
    val image: List<Image>?,
    val is_author: Boolean?,
    val is_favorite: Boolean?,
    val is_from_message: Int?,
    val need_mediascope_stat: Boolean?,
    val ov_id: String?,
    val owner_id: Long?,
    val player: String?,
    val processing: Int?,
    val repeat: Int?,
    val response_type: String?,
    val shape_id: Long?,
    val timeline_thumbs: TimelineThumbs?,
    val title: String?,
    val track_code: String?,
    val transcript_state: String?,
    val type: String?,
    val views: Int?,
    val width: Int?,
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Files(
        val failover_host: String?,
        val mp4_240: String?,
        val mp4_480: String?,
    )

    @JsonClass(generateAdapter = true)
    data class FirstFrame(
        val height: Int?,
        val url: String?,
        val width: Int?,
    )

    @JsonClass(generateAdapter = true)
    data class Image(
        val height: Int,
        val url: String,
        val width: Int,
        val with_padding: Int?,
    )

    @JsonClass(generateAdapter = true)
    data class TimelineThumbs(
        val count_per_image: Int?,
        val count_per_row: Int?,
        val count_total: Int?,
        val frame_height: Int?,
        val frame_width: Double?,
        val frequency: Int?,
        val is_uv: Boolean?,
        val links: List<String>?,
    )

    fun toDomain(): VkVideoMessageDomain = VkVideoMessageDomain(
        id = id,
        image = image.orEmpty().filter { it.width / it.height == 1 }.maxByOrNull { it.width }?.url
    )
}
