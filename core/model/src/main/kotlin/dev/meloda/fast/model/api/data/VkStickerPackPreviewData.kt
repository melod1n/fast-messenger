package dev.meloda.fast.model.api.data

import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkStickerPackPreviewDomain

@JsonClass(generateAdapter = true)
data class VkStickerPackPreviewData(
    val id: Long,
    val title: String,
    val description: String?,
    val author: String?,
    val icon: Icon?,
    val price: Price?,
    val can_purchase: Boolean,
    val url: String
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Icon(
        val base_url: String
    )

    @JsonClass(generateAdapter = true)
    data class Price(
        val current: Long,
        val regular: Long
    )

    fun toDomain(): VkStickerPackPreviewDomain = VkStickerPackPreviewDomain(
        id = id
    )
}

