package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkStickerDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkStickerData(
    @Json(name = "product_id") val productId: Int,
    @Json(name = "sticker_id") val stickerId: Int,
    @Json(name = "images") val images: List<Image>,
    @Json(name = "images_with_background") val imagesWithBackground: List<Image>,
    @Json(name = "animation_url") val animationUrl: String?,
    @Json(name = "animations") val animations: List<Animation>?
) {

    @JsonClass(generateAdapter = true)
    data class Image(
        @Json(name = "width") val width: Int,
        @Json(name = "height") val height: Int,
        @Json(name = "url") val url: String
    )

    @JsonClass(generateAdapter = true)
    data class Animation(
        @Json(name = "type") val type: String,
        @Json(name = "url") val url: String
    )

    fun toDomain() = VkStickerDomain(
        id = stickerId,
        productId = productId,
        images = images,
        backgroundImages = imagesWithBackground
    )
}
