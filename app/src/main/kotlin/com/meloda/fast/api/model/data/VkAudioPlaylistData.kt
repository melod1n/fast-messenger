package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkAudioPlaylistDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioPlaylistData(
    @Json(name = "id") val id: Int,
    @Json(name = "owner_id") val ownerId: Int,
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
