package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkStickerPackDomain

@JsonClass(generateAdapter = true)
data class VkStickerPackData(
    @Json(name = "id") val id: Long,
    @Json(name = "title") val title: String?,
    @Json(name = "purchased") val purchased: Int?,
    @Json(name = "active") val active: Int?,
    @Json(name = "stickers") val stickers: List<Sticker>?
) {

    @JsonClass(generateAdapter = true)
    data class Sticker(
        @Json(name = "sticker_id") val stickerId: Long,
        @Json(name = "is_allowed") val isAllowed: Boolean?
    )

    fun toDomain() = VkStickerPackDomain(
        id = id,
        title = title.orEmpty(),
        stickerIds = stickers.orEmpty()
            .filter { it.isAllowed != false }
            .map(VkStickerPackData.Sticker::stickerId)
    )
}
