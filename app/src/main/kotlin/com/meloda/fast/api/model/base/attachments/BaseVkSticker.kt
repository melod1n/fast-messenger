package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import androidx.annotation.IntDef
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.attachments.VkSticker
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkSticker(
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("sticker_id")
    val stickerId: Int,
    val images: List<Image>,
    @SerializedName("images_with_background")
    val imagesWithBackground: List<Image>,
    @SerializedName("animation_url")
    val animationUrl: String?,
    val animations: List<Animation>?
) : Parcelable {

    fun asVkSticker() = VkSticker(
        id = stickerId,
        productId = productId,
        images = images,
        backgroundImages = imagesWithBackground
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

@IntDef(64, 128, 256, 352)
annotation class StickerSize
