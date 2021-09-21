package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.model.base.attachments.BaseVkSticker
import com.meloda.fast.api.model.base.attachments.StickerSize
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkSticker(
    val id: Int,
    val productId: Int,
    val images: List<BaseVkSticker.Image>,
    val backgroundImages: List<BaseVkSticker.Image>
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

    fun urlForSize(@StickerSize size: Int): String? {
        for (image in images) {
            if (image.width == size) return image.url
        }

        return null
    }

}
