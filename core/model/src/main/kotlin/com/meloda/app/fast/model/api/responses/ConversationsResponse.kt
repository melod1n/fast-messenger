package com.meloda.app.fast.model.api.responses

import com.meloda.app.fast.model.api.data.VkContactData
import com.meloda.app.fast.model.api.data.VkConversationData
import com.meloda.app.fast.model.api.data.VkGroupData
import com.meloda.app.fast.model.api.data.VkMessageData
import com.meloda.app.fast.model.api.data.VkUserData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationsGetResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<ConversationsResponseItem>,
    @Json(name = "unread_count") val unreadCount: Int?,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class ConversationsResponseItem(
    @Json(name = "conversation") val conversation: VkConversationData,
    @Json(name = "last_message") val lastMessage: VkMessageData?
)

@JsonClass(generateAdapter = true)
data class ConversationsDeleteResponse(
    @Json(name = "last_deleted_id") val lastDeletedId: Int
)
