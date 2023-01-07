package com.meloda.fast.api.model.domain

import android.os.Parcelable
import android.text.SpannableString
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.ActionState
import com.meloda.fast.api.model.ConversationPeerType
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.ext.isFalse
import com.meloda.fast.ext.isTrue
import com.meloda.fast.model.base.Image
import com.meloda.fast.model.base.Text
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Suppress("MemberVisibilityCanBePrivate")
@Entity(tableName = "conversations")
@Parcelize
data class VkConversationDomain(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val localId: Int,
    val ownerId: Int?,
    val conversationTitle: String?,
    val conversationPhoto: String?,
    val isCallInProgress: Boolean,
    val isPhantom: Boolean,
    val lastConversationMessageId: Int,
    val inRead: Int,
    val outRead: Int,
    val lastMessageId: Int,
    val unreadCount: Int,
    val membersCount: Int?,
    val canChangePin: Boolean,
    val canChangeInfo: Boolean,
    val majorId: Int,
    val minorId: Int,
    val pinnedMessageId: Int?,
    val type: String,
) : Parcelable {

    @Ignore
    @IgnoredOnParcel
    var peerType: ConversationPeerType = ConversationPeerType.parse(type)

    @Ignore
    @IgnoredOnParcel
    var lastMessage: VkMessage? = null

    @Ignore
    @IgnoredOnParcel
    var pinnedMessage: VkMessage? = null

    @Ignore
    @IgnoredOnParcel
    var conversationUser: VkUser? = null

    @Ignore
    @IgnoredOnParcel
    var conversationGroup: VkGroup? = null

    @Ignore
    @IgnoredOnParcel
    var actionUser: VkUser? = null

    @Ignore
    @IgnoredOnParcel
    var actionGroup: VkGroup? = null

    @Ignore
    @IgnoredOnParcel
    var action: VkMessage.Action? = null

    @Ignore
    @IgnoredOnParcel
    var messageUser: VkUser? = null

    @Ignore
    @IgnoredOnParcel
    var messageGroup: VkGroup? = null

    fun isChat() = peerType.isChat()
    fun isUser() = peerType.isUser()
    fun isGroup() = peerType.isGroup()

    fun isInUnread() = inRead - lastMessageId < 0
    fun isOutUnread() = outRead - lastMessageId < 0

    fun isUnread() = isInUnread() || isOutUnread()

    fun isAccount() = id == UserConfig.userId

    fun isPinned() = majorId > 0

    fun extractAvatar(): Image {
        val placeholderImage = Image.ColorResource(R.color.colorOnPrimary)

        val avatarLink = when {
            peerType.isUser() -> conversationUser?.photo200
            peerType.isGroup() -> conversationGroup?.photo200
            peerType.isChat() -> conversationPhoto
            else -> null
        }

        val avatarImage = when {
            isAccount() -> null
            else -> avatarLink?.let { Image.Url(it) }
        } ?: placeholderImage

        return avatarImage
    }

    fun extractTitle(): Text {
        return when {
            isAccount() -> Text.Resource(R.string.favorites)
            peerType.isChat() -> Text.Simple(conversationTitle ?: "...")
            peerType.isUser() -> Text.Simple(conversationUser?.fullName ?: "...")
            peerType.isGroup() -> Text.Simple(conversationGroup?.name ?: "...")
            else -> Text.Simple("...")
        }
    }

    fun extractUnreadCounterText(): String? {
        if (lastMessage?.isOut.isFalse && !isInUnread()) return null

        return when (unreadCount) {
            in 1..999 -> unreadCount.toString()
            0 -> null
            else -> "%dK".format(unreadCount / 1000)
        }
    }

    // TODO: 07.01.2023, Danil Nikolaev: rewrite
    fun extractMessage(): SpannableString? {
        return SpannableString.valueOf(lastMessage?.text ?: "...")
    }

    fun extractReadCondition(): Boolean {
        return (lastMessage?.isOut.isTrue && isOutUnread()) ||
                (lastMessage?.isOut.isFalse && isInUnread())
    }

    fun mapToPresentation() = VkConversationUi(
        conversationId = id,
        lastMessageId = lastMessageId,
        avatar = extractAvatar(),
        title = extractTitle(),
        unreadCount = extractUnreadCounterText(),
        date = lastMessage?.date,
        message = extractMessage(),
        attachmentImage = null,
        isPinned = majorId > 0,
        actionState = ActionState.parse(isPhantom, isCallInProgress),
        isBirthday = false,
        isRead = extractReadCondition(),
        isAccount = isAccount(),
        isOnline = !isAccount() && conversationUser?.online == true,
        lastMessage = lastMessage,
        conversationUser = conversationUser,
        conversationGroup = conversationGroup,
        actionUser = actionUser,
        actionGroup = actionGroup,
        action = action,
        messageUser = messageUser,
        messageGroup = messageGroup,
        peerType = peerType
    )

}
