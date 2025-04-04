package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkGraffitiDomain

@JsonClass(generateAdapter = true)
data class VkGraffitiData(
    @Json(name = "id") val id: Long,
    @Json(name = "owner_id") val ownerId: Long,
    @Json(name = "url") val url: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int,
    @Json(name = "access_key") val accessKey: String
) {

    fun toDomain() = VkGraffitiDomain(
        id = id,
        ownerId = ownerId,
        url = url,
        width = width,
        height = height,
        accessKey = accessKey
    )
}
