package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkPodcastDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkPodcastData(
    @Json(name = "id") val id: Long,
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
