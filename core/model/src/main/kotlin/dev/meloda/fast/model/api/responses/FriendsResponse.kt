package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkUserData

@JsonClass(generateAdapter = true)
data class GetFriendsResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<VkUserData>,
    @Json(name = "contacts") val contacts: List<VkContactData>?
)
