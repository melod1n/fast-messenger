package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkGraffitiDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkGraffitiData(
    @Json(name = "id") val id: Int,
    @Json(name = "owner_id") val ownerId: Int,
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
