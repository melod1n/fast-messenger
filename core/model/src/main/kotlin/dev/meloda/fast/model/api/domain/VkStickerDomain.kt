package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkStickerData

data class VkStickerDomain(
    val id: Long,
    val productId: Long,
    val images: List<VkStickerData.Image>?,
    val backgroundImages: List<VkStickerData.Image>?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.STICKER

    val className: String = this::class.java.name

    fun urlForSize(size: Int): String? {
        for (image in images.orEmpty()) {
            if (image.width == size) return image.url
        }

        return null
    }
}
