package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkGiftDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkGiftData(
    @Json(name = "id") val id: Long,
    @Json(name = "thumb_256") val thumb256: String?,
    @Json(name = "thumb_96") val thumb96: String?,
    @Json(name = "thumb_48") val thumb48: String
) {

    fun toDomain() = VkGiftDomain(
        id = id,
        thumb256 = thumb256,
        thumb96 = thumb96,
        thumb48 = thumb48
    )
}
