package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkArticleDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkArticleData(
    @Json(name = "id") val id: Int
) : VkAttachmentData {

    fun toDomain(): VkArticleDomain = VkArticleDomain(
        id = id
    )
}
