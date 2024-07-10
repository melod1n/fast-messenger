package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType
import com.meloda.app.fast.model.api.data.VkStickerData

data class VkStickerDomain(
    val id: Int,
    val productId: Int,
    val images: List<VkStickerData.Image>,
    val backgroundImages: List<VkStickerData.Image>
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.STICKER

    val className: String = this::class.java.name

    fun urlForSize(size: Int): String? {
        for (image in images) {
            if (image.width == size) return image.url
        }

        return null
    }
}
