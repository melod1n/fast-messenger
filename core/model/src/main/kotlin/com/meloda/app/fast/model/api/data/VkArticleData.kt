package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkArticleDomain
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
