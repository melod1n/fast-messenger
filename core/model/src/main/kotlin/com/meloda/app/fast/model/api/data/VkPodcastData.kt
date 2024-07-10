package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkPodcastDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkPodcastData(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "artist") val artist: String,
    // ... other fields
) : VkAttachmentData {

    fun toDomain(): VkPodcastDomain = VkPodcastDomain(
        id = id,
        title = title,
        artist = artist
    )
}
