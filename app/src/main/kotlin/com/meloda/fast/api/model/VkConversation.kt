package com.meloda.fast.api.model

import androidx.lifecycle.MutableLiveData
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.api.UserConfig
import com.meloda.fast.model.SelectableItem
import kotlinx.parcelize.IgnoredOnParcel
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
    var unreadCount: Int,
    var membersCount: Int?,
    var canChangePin: Boolean,
    var majorId: Int,
    var minorId: Int,

    @Embedded(prefix = "pinnedMessage_")
    var pinnedMessage: VkMessage? = null,

    @Embedded(prefix = "lastMessage_")
    var lastMessage: VkMessage? = null,
) : SelectableItem(id) {

    @Ignore
    @IgnoredOnParcel
    val user = MutableLiveData<VkUser?>()

    @Ignore
    @IgnoredOnParcel
    val group = MutableLiveData<VkGroup?>()

    fun isChat() = type == "chat"
    fun isUser() = type == "user"
    fun isGroup() = type == "group"

    fun isInUnread() = inRead - lastMessageId < 0
    fun isOutUnread() = outRead - lastMessageId < 0

    fun isUnread() = isInUnread() || isOutUnread()

    fun isAccount() = id == UserConfig.userId

    fun isPinned() = majorId > 0

}
