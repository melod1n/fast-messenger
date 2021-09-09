package com.meloda.fast.api.network.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.BaseVKConversation
import com.meloda.fast.api.model.BaseVKMessage
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
    val conversation: BaseVKConversation,
    @SerializedName("last_message")
    val lastMessage: BaseVKMessage
) : Parcelable