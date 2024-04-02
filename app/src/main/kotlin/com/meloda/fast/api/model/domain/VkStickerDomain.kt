package com.meloda.fast.api.model.domain

import com.meloda.fast.api.model.data.VkStickerData


data class VkStickerDomain(
    val id: Int,
    val productId: Int,
    val images: List<VkStickerData.Image>,
    val backgroundImages: List<VkStickerData.Image>
) : VkAttachment {

    val className: String = this::class.java.name

    fun urlForSize(size: Int): String? {
        for (image in images) {
            if (image.width == size) return image.url
        }

        return null
    }
}
