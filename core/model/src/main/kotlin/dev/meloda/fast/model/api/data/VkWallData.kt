package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkWallDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkWallData(
    @Json(name = "id") val id: Int,
    @Json(name = "from_id") val from_id: Int,
    @Json(name = "to_id") val to_id: Int,
    @Json(name = "date") val date: Int,
    @Json(name = "text") val text: String,
    @Json(name = "attachments") val attachments: List<VkAttachmentItemData>?,
    @Json(name = "post_source") val post_source: PostSource?,
    @Json(name = "comments") val comments: Comments?,
    @Json(name = "likes") val likes: Likes?,
    @Json(name = "reposts") val reposts: Reposts?,
    @Json(name = "views") val views: Views?,
    @Json(name = "is_favorite") val is_favorite: Boolean,
    @Json(name = "donut") val donut: Donut?,
    @Json(name = "access_key") val access_key: String?,
    @Json(name = "short_text_rate") val short_text_rate: Double?
) {

    @JsonClass(generateAdapter = true)
    data class PostSource(
        val type: String,
        val platform: String?
    )

    @JsonClass(generateAdapter = true)
    data class Comments(
        val count: Int,
    )

    @JsonClass(generateAdapter = true)
    data class Likes(
        val count: Int
    )

    @JsonClass(generateAdapter = true)
    data class Reposts(
        val count: Int,
    )

    @JsonClass(generateAdapter = true)
    data class Views(
        val count: Int
    )

    @JsonClass(generateAdapter = true)
    data class Donut(
        val is_donut: Boolean
    )

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
}
