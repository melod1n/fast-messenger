package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkWall(
    val id: Int,
    @SerializedName("from_id")
    val fromId: Int,
    @SerializedName("to_id")
    val toId: Int,
    val date: Int,
    val text: String,
    val attachments: List<BaseVkAttachmentItem>?,
    @SerializedName("post_source")
    val postSource: PostSource,
    val comments: Comments,
    val likes: Likes,
    val reposts: Reposts,
    val views: Views,
    @SerializedName("is_favorite")
    val isFavorite: Boolean,
    val donut: Donut,
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("short_text_rate")
    val shortTextRate: Double
) : Parcelable {

    @Parcelize
    data class PostSource(
        val type: String,
        val platform: String
    ) : Parcelable

    @Parcelize
    data class Comments(
        val count: Int,
        @SerializedName("can_post")
        val canPost: Int,
        @SerializedName("groups_can_post")
        val groupsCanPost: Boolean
    ) : Parcelable

    @Parcelize
    data class Likes(
        val count: Int,
        @SerializedName("user_likes")
        val userLikes: Int,
        @SerializedName("can_like")
        val canLike: Int,
        @SerializedName("can_publish")
        val canPublish: Int,
    ) : Parcelable

    @Parcelize
    data class Reposts(
        val count: Int,
        @SerializedName("user_reposted")
        val userReposted: Int
    ) : Parcelable

    @Parcelize
    data class Views(
        val count: Int
    ) : Parcelable

    @Parcelize
    data class Donut(
        @SerializedName("is_donut")
        val isDonut: Boolean
    ) : Parcelable

}