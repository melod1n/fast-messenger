package dev.meloda.fast.model.api.responses

import dev.meloda.fast.model.api.data.VkAttachmentHistoryMessageData
import dev.meloda.fast.model.api.data.VkChatMemberData
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkConversationData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkMessageData
import dev.meloda.fast.model.api.data.VkUserData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesGetHistoryResponse(
    val count: Int,
    val items: List<VkMessageData>,
    val conversations: List<VkConversationData>?,
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?,
    val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class MessagesGetByIdResponse(
    val count: Int,
    val items: List<VkMessageData> = emptyList(),
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?
)

@JsonClass(generateAdapter = true)
data class MessagesGetConversationMembersResponse(
    val count: Int,
    val items: List<VkChatMemberData>?,
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?
)

@JsonClass(generateAdapter = true)
data class MessagesGetHistoryAttachmentsResponse(
    @Json(name = "items") val items: List<VkAttachmentHistoryMessageData>,
    @Json(name = "next_from") val nextFrom: String?,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
)
