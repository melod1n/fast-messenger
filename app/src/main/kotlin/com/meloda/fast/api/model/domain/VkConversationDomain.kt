package com.meloda.fast.api.model.domain

import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.ActionState
import com.meloda.fast.api.model.ConversationPeerType
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isFalse
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.asDrawable
import com.meloda.fast.model.base.parseString
import com.meloda.fast.util.TimeUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Calendar

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
    val inReadCmId: Int,
    val outReadCmId: Int,
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
    val interactionType: Int,
    val interactionIds: List<Int>
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

    fun isChat() = peerType.isChat()
    fun isUser() = peerType.isUser()
    fun isGroup() = peerType.isGroup()

    fun isInUnread() = inRead - lastMessageId < 0
    fun isOutUnread() = outRead - lastMessageId < 0

    fun isUnread() = isInUnread() || isOutUnread()

    fun isAccount() = id == UserConfig.userId

    fun isPinned() = majorId > 0

    fun extractAvatar(): UiImage {
        val placeholderImage = UiImage.Resource(R.drawable.ic_account_circle_cut)

        val avatarLink = when {
            peerType.isUser() -> {
                if (id == UserConfig.userId) {
                    null
                } else {
                    conversationUser?.photo200
                }
            }

            peerType.isGroup() -> conversationGroup?.photo200
            peerType.isChat() -> conversationPhoto
            else -> null
        }

        return avatarLink?.let(UiImage::Url) ?: placeholderImage
    }

    fun extractTitle(): String {
        return when {
            isAccount() -> UiText.Resource(R.string.favorites)
            peerType.isChat() -> UiText.Simple(conversationTitle ?: "...")
            peerType.isUser() -> UiText.Simple(conversationUser?.fullName ?: "...")
            peerType.isGroup() -> UiText.Simple(conversationGroup?.name ?: "...")
            else -> UiText.Simple("...")
        }.parseString(AppGlobal.Instance).orEmpty()
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
    fun extractMessage(): String {
        val actionMessage = VkUtils.getActionConversationText(
            message = lastMessage,
            youPrefix = "You",
            messageUser = lastMessage?.user,
            messageGroup = lastMessage?.group,
            action = lastMessage?.getPreparedAction(),
            actionUser = lastMessage?.actionUser,
            actionGroup = lastMessage?.actionGroup
        )

        val attachmentIcon: UiImage? = when {
            lastMessage?.text == null -> null
            !lastMessage?.forwards.isNullOrEmpty() -> {
                if (lastMessage?.forwards?.size == 1) {
                    UiImage.Resource(R.drawable.ic_attachment_forwarded_message)
                } else {
                    UiImage.Resource(R.drawable.ic_attachment_forwarded_messages)
                }
            }

            else -> VkUtils.getAttachmentConversationIcon(lastMessage)
        }

        val attachmentText = (if (attachmentIcon == null) VkUtils.getAttachmentText(
            message = lastMessage
        ) else null)

        val forwardsMessage = (if (lastMessage?.text == null) VkUtils.getForwardsText(
            message = lastMessage
        ) else null)

        val messageText =
            lastMessage?.text
                ?.let { VkUtils.prepareMessageText(it, forConversations = true) }
                ?.let { VkUtils.visualizeMentions(it, 0).toString() }
                ?.let(UiText::Simple)

        var prefix = when {
            actionMessage != null -> ""
            lastMessage?.isOut.isTrue -> "You: "
            else ->
                when {
                    lastMessage?.user != null && lastMessage?.user?.firstName?.isNotBlank().isTrue -> {
                        "${lastMessage?.user?.firstName}: "
                    }

                    lastMessage?.group != null && lastMessage?.group?.name?.isNotBlank().isTrue -> {
                        "${lastMessage?.group?.name}: "
                    }

                    else -> ""
                }
        }

        if ((!peerType.isChat() && lastMessage?.isOut.isFalse) || id == UserConfig.userId)
            prefix = ""

        val finalText =
            (actionMessage ?: forwardsMessage ?: attachmentText ?: messageText)
                ?.parseString(AppGlobal.Instance)
                ?.let(VkUtils::prepareMessageText)
                ?.let { text -> "$prefix$text" }


        return finalText.orDots()
    }

    fun extractAttachmentImage(): UiImage? {
        if (lastMessage?.text == null) return null
        return VkUtils.getAttachmentConversationIcon(lastMessage)
    }

    fun extractReadCondition(): Boolean {
        return (lastMessage?.isOut.isTrue && isOutUnread()) ||
                (lastMessage?.isOut.isFalse && isInUnread())
    }

    fun extractDate(): String {
        return TimeUtils.getLocalizedTime(AppGlobal.Instance, (lastMessage?.date ?: -1) * 1000L)
    }

    // TODO: 13.08.2023, Danil Nikolaev: rewrite
    fun extractInteractionUsers(): List<String> {
        return interactionIds.map { it.toString() }
    }

    // TODO: 05.08.2023, Danil Nikolaev: rewrite
    fun extractBirthday(): Boolean {
        val birthday = conversationUser?.birthday ?: return false
        val splitBirthday = birthday.split(".")

        return if (splitBirthday.size > 1) {
            val birthdayCalendar = Calendar.getInstance().apply {
                this[Calendar.DAY_OF_MONTH] = splitBirthday.first().toIntOrNull() ?: -1
                this[Calendar.MONTH] = (splitBirthday[1].toIntOrNull() ?: 0) - 1
            }
            val nowCalendar = Calendar.getInstance()

            (nowCalendar[Calendar.DAY_OF_MONTH] == birthdayCalendar[Calendar.DAY_OF_MONTH]
                    && nowCalendar[Calendar.MONTH] == birthdayCalendar[Calendar.MONTH])
        } else false
    }

    private fun extractInteractionText(): String? {
        val interactionType = InteractionType.parse(interactionType)
        val interactiveUsers = extractInteractionUsers()

        val typingText =
            if (interactionType == null) {
                null
            } else {
                if (!peerType.isChat() && interactiveUsers.size == 1) {
                    when (interactionType) {
                        InteractionType.File -> "Uploading file"
                        InteractionType.Photo -> "Uploading photo"
                        InteractionType.Typing -> "Typing"
                        InteractionType.Video -> "Uploading Video"
                        InteractionType.VoiceMessage -> "Recording voice message"
                    }
                } else {
                    "$interactiveUsers are typing"
                }
            }

        return typingText
    }

    fun copyWithEssentials(function: (VkConversationDomain) -> VkConversationDomain): VkConversationDomain {
        return function(this).also {
            it.lastMessage = this.lastMessage
            it.pinnedMessage = this.pinnedMessage
            it.conversationUser = this.conversationUser
            it.conversationGroup = this.conversationGroup
        }
    }

    fun mapToPresentation() = VkConversationUi(
        conversationId = id,
        lastMessageId = lastMessageId,
        avatar = extractAvatar(),
        title = extractTitle(),
        unreadCount = extractUnreadCounterText(),
        date = extractDate(),
        message = extractMessage(),
        attachmentImage = extractAttachmentImage(),
        isPinned = majorId > 0,
        actionState = ActionState.parse(isPhantom, isCallInProgress),
        isBirthday = extractBirthday(),
        isUnread = extractReadCondition(),
        isAccount = isAccount(),
        isOnline = !isAccount() && conversationUser?.online == true,
        lastMessage = lastMessage,
        conversationUser = conversationUser,
        conversationGroup = conversationGroup,
        peerType = peerType,
        interactionText = extractInteractionText(),
    )
}
