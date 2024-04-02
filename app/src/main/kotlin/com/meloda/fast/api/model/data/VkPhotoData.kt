package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkPhotoDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkPhotoData(
    @Json(name = "album_id") val albumId: Int,
    val date: Int,
    val id: Int,
    @Json(name = "owner_id") val ownerId: Int,
    @Json(name = "has_tags") val hasTags: Boolean,
    @Json(name = "access_key") val accessKey: String?,
    val sizes: List<Size>,
    val text: String?,
    @Json(name = "user_id") val userId: Int?,
    val lat: Double?,
    val long: Double?,
    @Json(name = "post_id") val postId: Int?
) : VkAttachmentData {

    fun toDomain() = VkPhotoDomain(
        albumId = albumId,
        date = date,
        id = id,
        ownerId = ownerId,
        hasTags = hasTags,
        accessKey = accessKey,
        sizes = sizes,
        text = text,
        userId = userId
    )

    @JsonClass(generateAdapter = true)
    data class Size(
        val height: Int,
        val width: Int,
        val type: String,
        val url: String
    )
}
