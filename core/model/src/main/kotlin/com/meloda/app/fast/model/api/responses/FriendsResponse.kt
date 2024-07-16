package dev.meloda.fast.model.api.responses

import dev.meloda.fast.model.api.data.VkUserData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetFriendsResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<VkUserData>
)
