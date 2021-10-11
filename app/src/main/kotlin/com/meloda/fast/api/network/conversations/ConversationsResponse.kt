package com.meloda.fast.api.network.conversations

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.base.BaseVkConversation
import com.meloda.fast.api.model.base.BaseVkGroup
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.BaseVkUser
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConversationsGetResponse(
    val count: Int,
    val items: List<ConversationsResponseItems>,
    @SerializedName("unread_count")
    val unreadCount: Int?,
    val profiles: List<BaseVkUser>?,
    val groups: List<BaseVkGroup>?
) : Parcelable

@Parcelize
data class ConversationsResponseItems(
    val conversation: BaseVkConversation,
    @SerializedName("last_message")
    val lastMessage: BaseVkMessage?
) : Parcelable