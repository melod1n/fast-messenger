package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkAudioPlaylistDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioPlaylistData(
    @Json(name = "id") val id: Long,
    @Json(name = "owner_id") val ownerId: Long,
    @Json(name = "type") val type: Int,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    // ... other fields
) : VkAttachmentData {

    fun toDomain(): VkAudioPlaylistDomain = VkAudioPlaylistDomain(
        id = id,
        ownerId = ownerId,
        title = title,
        description = description
    )
}
