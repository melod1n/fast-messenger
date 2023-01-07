package com.meloda.fast.api.model.data

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.domain.PeerType
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.model.base.AdapterDiffItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = "conversations")
@Parcelize
data class VkConversation(
    @PrimaryKey(autoGenerate = false)
    override var id: Int,
    var localId: Int,
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
    var canChangeInfo: Boolean,
    var majorId: Int,
    var minorId: Int,

    @Embedded(prefix = "pinnedMessage_")
    var pinnedMessage: VkMessage? = null,

    @Embedded(prefix = "lastMessage_")
    var lastMessage: VkMessage? = null,
) : Parcelable, AdapterDiffItem {

    fun mapToDomain(
        conversationUser: VkUser?,
        conversationGroup: VkGroup?,
        actionUser: VkUser?,
        actionGroup: VkGroup?,
        messageUser: VkUser?,
        messageGroup: VkGroup?,
    ) = VkConversationDomain(
        conversationId = id,
        messageId = lastMessageId,
        fromId = lastMessage?.fromId ?: -1,
        peerType = PeerType.parse(type),
        lastMessageId = lastMessageId,
        lastMessage = lastMessage,
        conversationTitle = title,
        conversationPhoto = photo200,
        unreadCount = unreadCount,
        majorId = majorId,
        isPhantom = isPhantom,
        isCallInProgress = callInProgress,
        inRead = inRead,
        outRead = outRead,
        conversationUser = conversationUser,
        conversationGroup = conversationGroup,
        actionUser = actionUser,
        actionGroup = actionGroup,
        action = lastMessage?.getPreparedAction(),
        messageUser = messageUser,
        messageGroup = messageGroup,
    )

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
