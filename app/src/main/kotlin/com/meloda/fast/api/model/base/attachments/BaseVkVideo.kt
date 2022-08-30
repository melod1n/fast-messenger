package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkVideo
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkVideo(
    val id: Int,
    val title: String,
    val width: Int,
    val height: Int,
    val duration: Int,
    val date: Int,
    val comments: Int,
    val description: String,
    val player: String,
    val added: Int,
    val type: String,
    val views: Int,
    val can_comment: Int,
    val can_edit: Int,
    val can_like: Int,
    val can_repost: Int,
    val can_subscribe: Int,
    val can_add_to_faves: Int,
    val can_add: Int,
    val can_attach_link: Int,
    val access_key: String?,
    val owner_id: Int,
    val ov_id: String,
    val is_favorite: Boolean,
    val track_code: String,
    val image: List<Image>,
    val first_frame: List<FirstFrame>,
    val files: File,
    val timeline_thumbs: TimelineThumbs,
    val ads: Ads
) : BaseVkAttachment() {

    fun asVkVideo() = VkVideo(
        id = id,
        ownerId = owner_id,
        images = image.map { it.asVideoImage() },
        firstFrames = first_frame,
        accessKey = access_key,
        title = title
    )

    @Parcelize
    data class Image(
        val width: Int,
        val height: Int,
        val url: String,
        val with_padding: Int?
    ) : Parcelable {

        fun asVideoImage() = VkVideo.VideoImage(
            width = width,
            height = height,
            url = url,
            withPadding = with_padding == 1
        )
    }

    @Parcelize
    data class FirstFrame(
        val height: Int,
        val width: Int,
        val url: String
    ) : Parcelable

    @Parcelize
    data class File(
        val mp4_240: String?,
        val mp4_360: String?,
        val mp4_480: String?,
        val mp4_720: String?,
        val mp4_1080: String?,
        val mp4_1440: String?,
        val hls: String,
        val dash_uni: String,
        val dash_sep: String,
        val hls_ondemand: String,
        val dash_ondemand: String,
        val failover_host: String
    ) : Parcelable

    @Parcelize
    data class TimelineThumbs(
        val count_per_image: Int,
        val count_per_row: Int,
        val count_total: Int,
        val frame_height: Int,
        val frame_width: Float,
        val links: List<String>,
        val is_uv: Boolean,
        val frequency: Int
    ) : Parcelable

    @Parcelize
    data class Ads(
        val slot_id: Int,
        val timeout: Int,
        val can_play: Int,
        val params: Params,
        val sections: List<String>,
        val midroll_percents: List<Float>
    ) : Parcelable {

        @Parcelize
        data class Params(
            val vk_id: Int,
            val duration: Int,
            val video_id: String,
            val pl: Int,
            val content_id: String,
            val lang: Int,
            val puid1: String,
            val puid2: Int,
            val puid3: Int,
            val puid5: Int,
            val puid6: Int,
            val puid7: Int,
            val puid9: Int,
            val puid10: Int,
            val puid12: Int,
            val puid13: Int,
            val puid14: Int,
            val puid15: Int,
            val puid18: Int,
            val puid21: Int,
            val sign: String,
            val groupId: Int,
            val vk_catid: Int,
            val is_xz_video: Int
        ) : Parcelable
    }

}