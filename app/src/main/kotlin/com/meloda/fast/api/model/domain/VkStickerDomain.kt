package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkStickerData
import com.meloda.fast.model.base.UiText

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

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_sticker)
}
