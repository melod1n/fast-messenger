package com.meloda.fast.api.network.messages

import android.os.Parcelable
import com.meloda.fast.api.model.base.BaseVkConversation
import com.meloda.fast.api.model.base.BaseVkGroup
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.BaseVkUser
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