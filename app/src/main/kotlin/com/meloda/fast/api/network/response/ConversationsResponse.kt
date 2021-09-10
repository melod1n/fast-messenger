package com.meloda.fast.api.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.base.BaseVkConversation
import com.meloda.fast.api.model.base.BaseVkMessage
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConversationsGetResponse(
    val count: Int,
    val items: List<ConversationsResponseItems>,
    @SerializedName("unread_count")
    val unreadCount: Int?
) : Parcelable

@Parcelize
data class ConversationsResponseItems(
    val conversation: BaseVkConversation,
    @SerializedName("last_message")
    val lastMessage: BaseVkMessage
) : Parcelable