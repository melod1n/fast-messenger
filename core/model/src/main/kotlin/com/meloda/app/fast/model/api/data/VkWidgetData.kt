package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkWidgetDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkWidgetData(
    val id: Int
) : VkAttachmentData {

    fun toDomain() = VkWidgetDomain(id)
}
