package com.meloda.fast.api.network.messages

import android.os.Parcelable
import com.meloda.fast.api.model.base.*
import com.meloda.fast.api.model.data.BaseVkConversation
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessagesGetHistoryResponse(
    val count: Int,
    val items: List<BaseVkMessage> = emptyList(),
    val conversations: List<BaseVkConversation>?,
    val profiles: List<BaseVkUser>?,
    val groups: List<BaseVkGroup>?
) : Parcelable

@Parcelize
data class MessagesGetByIdResponse(
    val count: Int,
    val items: List<BaseVkMessage> = emptyList(),
    val profiles: List<BaseVkUser>?,
    val groups: List<BaseVkGroup>?
) : Parcelable

@Parcelize
data class MessagesGetConversationMembersResponse(
    val count: Int,
    val items: List<BaseVkChatMember> = emptyList(),
    val profiles: List<BaseVkUser>?,
    val groups: List<BaseVkGroup>?
) : Parcelable
