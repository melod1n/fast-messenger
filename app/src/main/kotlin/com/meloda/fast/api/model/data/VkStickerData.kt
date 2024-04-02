package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkStickerDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkStickerData(
    @Json(name = "product_id") val productId: Int,
    @Json(name = "sticker_id") val stickerId: Int,
    @Json(name = "images") val images: List<Image>,
    @Json(name = "images_with_background") val imagesWithBackground: List<Image>,
    @Json(name = "animation_url") val animationUrl: String?,
    val animations: List<Animation>?
) {

    fun toDomain() = VkStickerDomain(
        id = stickerId,
        productId = productId,
        images = images,
        backgroundImages = imagesWithBackground
    )

    @JsonClass(generateAdapter = true)
    data class Image(
        val width: Int,
        val height: Int,
        val url: String
    )

    @JsonClass(generateAdapter = true)
    data class Animation(
        val type: String,
        val url: String
    )
}
