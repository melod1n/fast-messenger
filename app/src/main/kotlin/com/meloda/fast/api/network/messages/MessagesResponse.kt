package com.meloda.fast.api.network.messages

import com.meloda.fast.api.model.data.VkChatMemberData
import com.meloda.fast.api.model.data.VkConversationData
import com.meloda.fast.api.model.data.VkGroupData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.model.data.VkUserData
import com.squareup.moshi.JsonClass


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
    val items: List<VkChatMemberData> = emptyList(),
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?
)
