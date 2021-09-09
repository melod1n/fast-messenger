package com.meloda.fast.api.model.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VKVideoAttachment(
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
    @SerializedName("can_comment")
    val canComment: Int,
    @SerializedName("can_edit")
    val canEdit: Int,
    @SerializedName("can_like")
    val canLike: Int,
    @SerializedName("can_repost")
    val canRepost: Int,
    @SerializedName("can_subscribe")
    val canSubscribe: Int,
    @SerializedName("can_add_to_faves")
    val canAddToFaves: Int,
    @SerializedName("can_add")
    val canAdd: Int,
    @SerializedName("can_attach_link")
    val canAttachLink: Int,
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("ov_id")
    val ovId: String,
    @SerializedName("is_favorite")
    val isFavorite: Boolean,
    @SerializedName("track_code")
    val trackCode: String,
    val image: List<Image>,
    @SerializedName("first_frame")
    val firstFrame: List<FirstFrame>,
    val files: List<File>,
    @SerializedName("timeline_thumbs")
    val timelineThumbs: TimelineThumbs
    //ads
) : BaseVKAttachment() {

    @Parcelize
    data class Image(
        val height: Int,
        val width: Int,
        val url: String,
        @SerializedName("with_padding")
        val withPadding: Int
    ) : Parcelable

    @Parcelize
    data class FirstFrame(
        val height: Int,
        val width: Int,
        val url: String
    ) : Parcelable

    @Parcelize
    data class File(
        val mp4_240: String,
        val mp4_360: String,
        val mp4_480: String,
        val mp4_720: String,
        val mp4_1080: String,
        val mp4_1440: String,
        val hls: String,
        @SerializedName("dash_uni")
        val dashUni: String,
        @SerializedName("dash_sep")
        val dashSep: String,
        @SerializedName("hls_ondemand")
        val hlsOnDemand: String,
        @SerializedName("dash_ondemand")
        val dashOnDemand: String,
        @SerializedName("failover_host")
        val failOverHost: String
    ) : Parcelable

    @Parcelize
    data class TimelineThumbs(
        @SerializedName("count_per_image")
        val countPerImage: Int,
        @SerializedName("count_per_row")
        val countPerRow: Int,
        @SerializedName("count_total")
        val countTotal: Int,
        @SerializedName("frame_height")
        val frameHeight: Int,
        @SerializedName("frame_width")
        val frameWidth: Float,
        val links: List<String>,
        @SerializedName("is_uv")
        val isUv: Boolean,
        val frequency: Int
    ) : Parcelable

}