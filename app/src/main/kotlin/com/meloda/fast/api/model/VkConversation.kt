package com.meloda.fast.api.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "conversations")
@Parcelize
data class VkConversation(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var ownerId: Int?,
    var title: String?,
    var photo200: String?,
    var type: String,
    var callInProgress: Boolean,
    var isPhantom: Boolean,
    var lastConversationMessageId: Int,
    var inRead: Int,
    var outRead: Int,
    var isMarkedUnread: Boolean,
    var lastMessageId: Int,
    var unreadCount: Int?,
    var membersCount: Int?,
    var isPinned: Boolean,
    var canChangePin: Boolean,

    @Embedded(prefix = "pinnedMessage_")
    var pinnedMessage: VkMessage? = null,

    @Embedded(prefix = "lastMessage_")
    var lastMessage: VkMessage? = null,
) : Parcelable {

    fun isChat() = type == "chat"
    fun isUser() = type == "user"
    fun isGroup() = type == "group"

    fun isInUnread() = inRead < lastMessageId
    fun isOutUnread() = outRead < lastMessageId

    fun isUnread() = isInUnread() || isOutUnread()

}
