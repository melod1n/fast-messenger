package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkGroupStickerDomain

@JsonClass(generateAdapter = true)
data class VkGroupStickerData(
    val id: Long,
    val owner_id: Long,
    val pack_id: Long?,
    val status: String?,
    val is_deleted: Boolean?,
    val images: List<Image>?
): VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Image(
        @Json(name = "width") val width: Int,
        @Json(name = "height") val height: Int,
        @Json(name = "url") val url: String
    )

    fun toDomain(): VkGroupStickerDomain = VkGroupStickerDomain(
        id = id
    )
}
