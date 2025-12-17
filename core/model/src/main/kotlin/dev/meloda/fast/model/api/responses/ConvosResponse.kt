package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkConvoData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkMessageData
import dev.meloda.fast.model.api.data.VkUserData

@JsonClass(generateAdapter = true)
data class ConvosGetResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<ConvosResponseItem>,
    @Json(name = "unread_count") val unreadCount: Int?,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class ConvosGetByIdResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<VkConvoData>,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class ConvosResponseItem(
    @Json(name = "conversation") val convo: VkConvoData,
    @Json(name = "last_message") val lastMessage: VkMessageData?
)

@JsonClass(generateAdapter = true)
data class ConvosDeleteResponse(
    @Json(name = "last_deleted_id") val lastDeletedId: Long
)
