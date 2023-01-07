package com.meloda.fast.api.model.domain

import android.text.SpannableString
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.data.ActionState
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.ext.isFalse
import com.meloda.fast.ext.isTrue
import com.meloda.fast.model.base.Image
import com.meloda.fast.model.base.Text

@Suppress("MemberVisibilityCanBePrivate")
data class VkConversationDomain(
    val conversationId: Int,
    val messageId: Int,
    val fromId: Int,
    val peerType: PeerType,
    val lastMessageId: Int,
    val lastMessage: VkMessage?,
    val conversationTitle: String?,
    val conversationPhoto: String?,
    val unreadCount: Int,
    val majorId: Int,
    val isPhantom: Boolean,
    val isCallInProgress: Boolean,
    val inRead: Int,
    val outRead: Int,
    val conversationUser: VkUser?,
    val conversationGroup: VkGroup?,
    val actionUser: VkUser?,
    val actionGroup: VkGroup?,
    val action: VkMessage.Action?,
    val messageUser: VkUser?,
    val messageGroup: VkGroup?,
) {

    fun isAccount() = peerType.isUser() && conversationId == UserConfig.userId

    fun isInUnread() = inRead - lastMessageId < 0

    fun isOutUnread() = outRead - lastMessageId < 0

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
        conversationId = conversationId,
        messageId = messageId,
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

sealed class PeerType {
    object User : PeerType()
    object Group : PeerType()
    object Chat : PeerType()

    fun isUser() = this == User
    fun isGroup() = this == Group
    fun isChat() = this == Chat

    companion object {
        fun parse(type: String): PeerType {
            return when (type) {
                "user" -> User
                "group" -> Group
                else -> Chat
            }
        }
    }
}
