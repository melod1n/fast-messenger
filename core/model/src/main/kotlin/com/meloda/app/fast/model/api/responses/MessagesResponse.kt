package com.meloda.app.fast.model.api.responses

import com.meloda.app.fast.model.api.data.VkChatMemberData
import com.meloda.app.fast.model.api.data.VkConversationData
import com.meloda.app.fast.model.api.data.VkGroupData
import com.meloda.app.fast.model.api.data.VkMessageData
import com.meloda.app.fast.model.api.data.VkUserData
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MessagesGetHistoryResponse(
    val count: Int,
    val items: List<VkMessageData> = emptyList(),
    val conversations: List<VkConversationData>?,
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?
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
