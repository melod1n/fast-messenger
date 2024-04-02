package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkWidgetDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkWidgetData(
    val id: Int
) : VkAttachmentData {

    fun toDomain() = VkWidgetDomain(id)
}
