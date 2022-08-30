package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkWall
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkWall(
    val id: Int,
    val from_id: Int,
    val to_id: Int,
    val date: Int,
    val text: String,
    val attachments: List<BaseVkAttachmentItem>?,
    val post_source: PostSource?,
    val comments: Comments?,
    val likes: Likes?,
    val reposts: Reposts?,
    val views: Views?,
    val is_favorite: Boolean,
    val donut: Donut?,
    val access_key: String?,
    val short_text_rate: Double
) : Parcelable {

    fun asVkWall() = VkWall(
        id = id,
        fromId = from_id,
        toId = to_id,
        date = date,
        text = text,
        attachments = attachments,
        comments = comments?.count,
        likes = likes?.count,
        reposts = reposts?.count,
        views = views?.count,
        isFavorite = is_favorite,
        accessKey = access_key
    )

    @Parcelize
    data class PostSource(
        val type: String,
        val platform: String
    ) : Parcelable

    @Parcelize
    data class Comments(
        val count: Int,
        val can_post: Int,
        val groups_can_post: Boolean
    ) : Parcelable

    @Parcelize
    data class Likes(
        val count: Int,
        val user_likes: Int,
        val can_like: Int,
        val can_publish: Int,
    ) : Parcelable

    @Parcelize
    data class Reposts(
        val count: Int,
        val user_reposted: Int
    ) : Parcelable

    @Parcelize
    data class Views(
        val count: Int
    ) : Parcelable

    @Parcelize
    data class Donut(
        val is_donut: Boolean
    ) : Parcelable

}