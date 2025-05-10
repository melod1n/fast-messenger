package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.PhotoSize
import dev.meloda.fast.model.api.domain.VkPhotoDomain

@JsonClass(generateAdapter = true)
data class VkPhotoData(
    @Json(name = "album_id") val albumId: Long,
    @Json(name = "date") val date: Int?,
    @Json(name = "id") val id: Long,
    @Json(name = "owner_id") val ownerId: Long,
    @Json(name = "has_tags") val hasTags: Boolean?,
    @Json(name = "access_key") val accessKey: String?,
    @Json(name = "sizes") val sizes: List<Size>,
    @Json(name = "text") val text: String?,
    @Json(name = "user_id") val userId: Long?,
    @Json(name = "lat") val lat: Double?,
    @Json(name = "long") val long: Double?,
    @Json(name = "post_id") val postId: Long?
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Size(
        val height: Int,
        val width: Int,
        val type: String,
        val url: String
    )

    fun toDomain() = VkPhotoDomain(
        albumId = albumId,
        date = date,
        id = id,
        ownerId = ownerId,
        hasTags = hasTags == true,
        accessKey = accessKey,
        sizes = sizes.map { size ->
            PhotoSize(
                height = size.height,
                width = size.width,
                type = size.type,
                url = size.url
            )
        },
        text = text,
        userId = userId
    )
}
