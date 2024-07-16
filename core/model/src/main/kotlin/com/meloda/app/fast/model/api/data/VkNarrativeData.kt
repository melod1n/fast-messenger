package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkNarrativeDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkNarrativeData(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?
) : VkAttachmentData {

    fun toDomain(): VkNarrativeDomain = VkNarrativeDomain(
        id = id,
        title = title
    )
}
