package com.meloda.fast.api.network.messages

import android.os.Parcelable
import com.meloda.fast.api.model.base.*
import com.meloda.fast.api.model.data.VkConversationData
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize


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
    val items: List<BaseVkChatMember> = emptyList(),
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?
)
