package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.VkStickerPackData

@JsonClass(generateAdapter = true)
data class StoreGetProductsResponse(
    @Json(name = "count") val count: Int?,
    @Json(name = "items") val items: List<VkStickerPackData>?
)
