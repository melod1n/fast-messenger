package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkWidgetDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkWidgetData(
    val id: Long
) : VkAttachmentData {

    fun toDomain() = VkWidgetDomain(id)
}
