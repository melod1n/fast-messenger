package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkSticker
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkSticker(
    val product_id: Int,
    val sticker_id: Int,
    val images: List<Image>,
    val images_with_background: List<Image>,
    val animation_url: String?,
    val animations: List<Animation>?
) : Parcelable {

    fun asVkSticker() = VkSticker(
        id = sticker_id,
        productId = product_id,
        images = images,
        backgroundImages = images_with_background
    )

    @Parcelize
    data class Image(
        val width: Int,
        val height: Int,
        val url: String
    ) : Parcelable

    @Parcelize
    data class Animation(
        val type: String,
        val url: String
    ) : Parcelable

}
