package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkArtistDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkArtistData(
    @Json(name = "name") val name: String,
    @Json(name = "id") val id: String,
    @Json(name = "photo") val photo: List<PhotoSize>?,
    @Json(name = "popular_audios_block_id") val popularAudiosBlockId: String?
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class PhotoSize(
        val height: Int,
        val width: Int,
        val url: String
    )

    fun toDomain(): VkArtistDomain = VkArtistDomain(
        id = id,
        name = name,
        photos = photo.orEmpty()
    )
}
