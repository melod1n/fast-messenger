package com.meloda.fast.api.model.domain

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.google.common.collect.ImmutableList
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkUsersMap
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isFalse
import com.meloda.fast.ext.isTrue
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString
import com.meloda.fast.util.TimeUtils
import java.util.Calendar

@Immutable
data class VkConversationDomain(
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
    val interactionIds: List<Int>,
    val peerType: ConversationPeerType = ConversationPeerType.parse(type),
    val lastMessage: VkMessageDomain? = null,
    val pinnedMessage: VkMessageDomain? = null,
    val conversationUser: VkUserDomain? = null,
    val conversationGroup: VkGroupDomain? = null
) {


    fun isChat() = peerType.isChat()
    fun isUser() = peerType.isUser()
    fun isGroup() = peerType.isGroup()

    fun isInUnread() = inRead - lastMessageId < 0
    fun isOutUnread() = outRead - lastMessageId < 0

    fun isUnread() = isInUnread() || isOutUnread()

    fun isAccount() = id == UserConfig.userId

    fun isPinned() = majorId > 0

    fun getUserAndGroup(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        val user: VkUserDomain? = usersMap.conversationUser(this)
        val group: VkGroupDomain? = groupsMap.conversationGroup(this)

        return user to group
    }

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
    fun extractMessage(): AnnotatedString {
        val actionMessage = VkUtils.getActionMessageText(
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
            !lastMessage.forwards.isNullOrEmpty() -> {
                if (lastMessage.forwards.size == 1) {
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

        val messageText = lastMessage?.text.orEmpty()


        val prefixText: AnnotatedString? = when {
            actionMessage != null -> null

            lastMessage == null -> null

            id == UserConfig.userId -> null

            !peerType.isChat() && !lastMessage.isOut -> null

            lastMessage.isOut -> buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(
                        UiText.Resource(R.string.you_message_prefix).parseString(AppGlobal.Instance)
                    )
                }
            }

            else ->
                when {
                    lastMessage.user?.firstName.orEmpty().isNotEmpty() -> buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(lastMessage.user?.firstName)
                        }
                    }

                    lastMessage.group?.name.orEmpty().isNotEmpty() -> buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(lastMessage.group?.name)
                        }
                    }

                    else -> null
                }
        }

        val prefix = buildAnnotatedString {
            if (prefixText != null) {
                append(prefixText)
                append(": ")
            }
        }

        val finalText = when {
            actionMessage != null -> {
                prefix + actionMessage
            }

            forwardsMessage != null -> {
                prefix + forwardsMessage
            }

            attachmentText != null -> {
                prefix + attachmentText
            }

            else ->
                messageText
                    .let { text -> VkUtils.prepareMessageText(text, true) }
                    .let { text -> VkUtils.getTextWithVisualizedMentions(text, Color.Red) }
                    .let { text -> prefix + text }
        }

        return finalText
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
        isOnline = !isAccount() && conversationUser?.onlineStatus?.isOnline() == true,
        lastMessage = lastMessage,
        conversationUser = conversationUser,
        conversationGroup = conversationGroup,
        peerType = peerType,
        interactionText = extractInteractionText(),
        isExpanded = false,
        options = ImmutableList.of()
    )

    enum class ActionState {
        PHANTOM, CALL_IN_PROGRESS, NONE;

        companion object {
            fun parse(isPhantom: Boolean, isCallInProgress: Boolean): ActionState {
                return when {
                    isPhantom -> PHANTOM
                    isCallInProgress -> CALL_IN_PROGRESS
                    else -> NONE
                }
            }
        }
    }

    enum class ConversationPeerType {
        USER, GROUP, CHAT;

        fun isUser(): Boolean = this == USER
        fun isGroup(): Boolean = this == GROUP
        fun isChat(): Boolean = this == CHAT

        companion object {
            fun parse(type: String): ConversationPeerType {
                return when (type) {
                    "user" -> USER
                    "group" -> GROUP
                    "chat" -> CHAT
                    else -> error("Unknown type: $type")
                }
            }
        }
    }
}
