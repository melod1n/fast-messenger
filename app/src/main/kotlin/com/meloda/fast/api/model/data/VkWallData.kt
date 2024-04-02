package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkWallDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkWallData(
    val id: Int,
    val from_id: Int,
    val to_id: Int,
    val date: Int,
    val text: String,
    val attachments: List<VkAttachmentItemData>?,
    val post_source: PostSource?,
    val comments: Comments?,
    val likes: Likes?,
    val reposts: Reposts?,
    val views: Views?,
    val is_favorite: Boolean,
    val donut: Donut?,
    val access_key: String?,
    val short_text_rate: Double
) {

    fun toDomain() = VkWallDomain(
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

    @JsonClass(generateAdapter = true)
    data class PostSource(
        val type: String,
        val platform: String
    )

    @JsonClass(generateAdapter = true)
    data class Comments(
        val count: Int,
        val can_post: Int,
        val groups_can_post: Boolean
    )

    @JsonClass(generateAdapter = true)
    data class Likes(
        val count: Int,
        val user_likes: Int,
        val can_like: Int,
        val can_publish: Int,
    )

    @JsonClass(generateAdapter = true)
    data class Reposts(
        val count: Int,
        val user_reposted: Int
    )

    @JsonClass(generateAdapter = true)
    data class Views(
        val count: Int
    )

    @JsonClass(generateAdapter = true)
    data class Donut(
        val is_donut: Boolean
    )
}
