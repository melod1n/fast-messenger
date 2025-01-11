package dev.meloda.fast.conversations.util

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.conena.nanokt.jvm.util.dayOfMonth
import com.conena.nanokt.jvm.util.month
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.model.UiText
import dev.meloda.fast.common.model.parseString
import dev.meloda.fast.common.util.TimeUtils
import dev.meloda.fast.conversations.model.ActionState
import dev.meloda.fast.conversations.model.UiConversation
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.util.ImmutableList
import java.util.Calendar
import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow
import dev.meloda.fast.ui.R as UiR

fun VkConversation.asPresentation(
    resources: Resources,
    useContactName: Boolean
): UiConversation = UiConversation(
    id = id,
    lastMessageId = lastMessageId,
    avatar = extractAvatar(),
    title = extractTitle(this, useContactName, resources),
    unreadCount = extractUnreadCount(lastMessage, this),
    date = TimeUtils.getLocalizedTime(resources, (lastMessage?.date ?: -1) * 1000L),
    message = extractMessage(resources, lastMessage, id, peerType),
    attachmentImage = if (lastMessage?.text == null) null
    else getAttachmentConversationIcon(lastMessage),
    isPinned = majorId > 0,
    actionImageId = ActionState.parse(isPhantom, isCallInProgress).getResourceId(),
    isBirthday = extractBirthday(this),
    isUnread = extractReadCondition(this, lastMessage),
    isAccount = isAccount(id),
    isOnline = !isAccount(id) && user?.onlineStatus?.isOnline() == true,
    lastMessage = lastMessage,
    peerType = peerType,
    interactionText = extractInteractionText(resources, this),
    isExpanded = false,
    options = ImmutableList.empty()
)

fun VkConversation.extractAvatar() = when (peerType) {
    PeerType.USER -> {
        if (isAccount(id)) null
        else user?.photo200
    }

    PeerType.GROUP -> {
        group?.photo200
    }

    PeerType.CHAT -> {
        photo200
    }
}?.let(UiImage::Url) ?: UiImage.Resource(UiR.drawable.ic_account_circle_cut)

private fun extractTitle(
    conversation: VkConversation,
    useContactName: Boolean,
    resources: Resources
) = when (conversation.peerType) {
    PeerType.USER -> {
        if (isAccount(conversation.id)) {
            UiText.Resource(UiR.string.favorites)
        } else {
            val userName = conversation.user?.let { user ->
                if (useContactName) {
                    VkMemoryCache.getContact(user.id)?.name ?: user.fullName
                } else {
                    user.fullName
                }
            }

            UiText.Simple(userName.orDots())
        }
    }

    PeerType.GROUP -> UiText.Simple(conversation.group?.name.orDots())
    PeerType.CHAT -> UiText.Simple(conversation.title.orDots())
}.parseString(resources).orDots()

private fun extractUnreadCount(
    lastMessage: VkMessage?,
    conversation: VkConversation
): String? = when {
    lastMessage?.isOut == false && !conversation.isInUnread() -> null
    conversation.unreadCount == 0 -> null
    conversation.unreadCount < 1000 -> conversation.unreadCount.toString()
    else -> {
        val exp = (ln(conversation.unreadCount.toDouble()) / ln(1000.0)).toInt()
        val suffix = "KMBT"[exp - 1]

        val result = conversation.unreadCount / 1000.0.pow(exp.toDouble())

        if (result.toLong().toDouble() == result) {
            String.format(Locale.getDefault(), "%.0f%s", result, suffix)
        } else {
            String.format(Locale.getDefault(), "%.1f%s", result, suffix)
        }
    }
}

private fun extractMessage(
    resources: Resources,
    lastMessage: VkMessage?,
    peerId: Int,
    peerType: PeerType
): AnnotatedString {
    val youPrefix = UiText.Resource(UiR.string.you_message_prefix)
        .parseString(resources)
        .orDots()

    val actionMessage = extractActionText(
        lastMessage = lastMessage,
        resources = resources,
        youPrefix = youPrefix
    )

    val attachmentIcon: UiImage? = extractAttachmentIcon(lastMessage)

    val attachmentText: AnnotatedString? =
        if (attachmentIcon != null) null
        else extractAttachmentText(resources, lastMessage)

    val forwardsMessage =
        if (lastMessage?.text != null) null
        else extractForwardsText(resources, lastMessage)

    val messageText = lastMessage?.text.orEmpty()

    val prefixText: AnnotatedString? = when {
        actionMessage != null -> null

        lastMessage == null -> null

        peerId == UserConfig.userId -> null

        !peerType.isChat() && !lastMessage.isOut -> null

        lastMessage.isOut -> buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(youPrefix)
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
                .replace("\n", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("<br>", " ")
                .replace("&gt;", ">")
                .replace("&lt;", "<")
                .replace("<br/>", " ")
                .replace("&ndash;", "-")
                .trim()
                .let { text -> getTextWithVisualizedMentions(text, Color.Red) }
                .let { text -> prefix + text }
    }

    return finalText
}

private fun extractActionText(
    lastMessage: VkMessage?,
    resources: Resources,
    youPrefix: String
): AnnotatedString? {
    if (lastMessage == null) return null

    val fromId = lastMessage.fromId
    val text = lastMessage.actionText.orDots()
    val groupName = lastMessage.group?.name.orDots()
    val userName = lastMessage.user?.fullName.orDots()
    val actionGroupName = lastMessage.actionGroup?.name.orDots()
    val actionUserName = lastMessage.actionUser?.fullName.orDots()
    val memberId = lastMessage.actionMemberId
    val isMemberUser = (memberId ?: 0) > 0
    val isMemberGroup = (memberId ?: 0) < 0

    val prefix = when {
        lastMessage.fromId == UserConfig.userId -> youPrefix
        lastMessage.isGroup() -> groupName
        lastMessage.isUser() -> userName
        else -> null
    }.orDots()

    val memberPrefix = when {
        memberId == UserConfig.userId -> youPrefix
        isMemberUser -> actionUserName
        isMemberGroup -> actionGroupName
        else -> null
    }.orDots()

    return buildAnnotatedString {
        when (lastMessage.action) {
            null -> return null

            VkMessage.Action.CHAT_CREATE -> {
                val string = UiText.ResourceParams(
                    UiR.string.message_action_chat_created,
                    listOf(prefix, text)
                ).parseString(resources).orEmpty()

                append(string)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )

                val textStartIndex = string.indexOf(text)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = textStartIndex,
                    end = textStartIndex + text.length
                )
            }

            VkMessage.Action.CHAT_TITLE_UPDATE -> {
                val string = UiText.ResourceParams(
                    UiR.string.message_action_chat_renamed,
                    listOf(prefix, text)
                ).parseString(resources).orEmpty()

                append(string)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )

                val textStartIndex = string.indexOf(text)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = textStartIndex,
                    end = textStartIndex + text.length
                )
            }

            VkMessage.Action.CHAT_PHOTO_UPDATE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_photo_update,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_PHOTO_REMOVE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_photo_remove,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_KICK_USER -> {
                if (memberId == fromId) {
                    UiText.ResourceParams(
                        UiR.string.message_action_chat_user_left,
                        listOf(memberPrefix)
                    ).parseString(resources).orEmpty().let(::append)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.SemiBold),
                        start = 0,
                        end = memberPrefix.length
                    )
                } else {
                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else lastMessage.actionUser.toString()

                    val string = UiText.ResourceParams(
                        UiR.string.message_action_chat_user_kicked,
                        listOf(prefix, postfix)
                    ).parseString(resources).orEmpty()

                    append(string)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.SemiBold),
                        start = 0,
                        end = prefix.length
                    )

                    val postfixStartIndex = string.indexOf(postfix)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.SemiBold),
                        start = postfixStartIndex,
                        end = postfixStartIndex + postfix.length
                    )
                }
            }

            VkMessage.Action.CHAT_INVITE_USER -> {
                if (memberId == lastMessage.fromId) {
                    UiText.ResourceParams(
                        UiR.string.message_action_chat_user_returned,
                        listOf(memberPrefix)
                    ).parseString(resources).orEmpty().let(::append)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.SemiBold),
                        start = 0,
                        end = memberPrefix.length
                    )
                } else {
                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else lastMessage.actionUser.toString()

                    val string = UiText.ResourceParams(
                        UiR.string.message_action_chat_user_invited,
                        listOf(memberPrefix, postfix)
                    ).parseString(resources).orEmpty()

                    append(string)

                    val postfixStartIndex = string.indexOf(postfix)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.SemiBold),
                        start = postfixStartIndex,
                        end = postfixStartIndex + postfix.length
                    )
                }
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_LINK -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_user_joined_by_link,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_user_joined_by_call,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_user_joined_by_call_link,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_PIN_MESSAGE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_pin_message,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_unpin_message,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_SCREENSHOT -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_screenshot,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_STYLE_UPDATE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_style_update,
                    listOf(prefix)
                ).parseString(resources).orEmpty().let(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.SemiBold),
                    start = 0,
                    end = prefix.length
                )
            }
        }
    }
}

private fun extractAttachmentIcon(
    lastMessage: VkMessage?
): UiImage? = when {
    lastMessage == null -> null
    lastMessage.text == null -> null
    !lastMessage.forwards.isNullOrEmpty() -> {
        if (lastMessage.forwards.orEmpty().size == 1) {
            UiImage.Resource(UiR.drawable.ic_attachment_forwarded_message)
        } else {
            UiImage.Resource(UiR.drawable.ic_attachment_forwarded_messages)
        }
    }

    else -> {
        lastMessage.attachments?.let { attachments ->
            if (attachments.isEmpty()) return null
            if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
                lastMessage.geoType?.let {
                    return UiImage.Resource(UiR.drawable.ic_map_marker)
                }

                getAttachmentIconByType(attachments.first().type)
            } else {
                UiImage.Resource(UiR.drawable.ic_baseline_attach_file_24)
            }
        }
    }
}

private fun extractAttachmentText(
    resources: Resources,
    lastMessage: VkMessage?
): AnnotatedString? = when {
    lastMessage == null -> null
    lastMessage.geoType != null -> {
        buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                when (lastMessage.geoType) {
                    "point" -> {
                        UiText.Resource(UiR.string.message_geo_point)
                            .parseString(resources)
                            .let(::append)
                    }

                    else -> {
                        UiText.Resource(UiR.string.message_geo)
                            .parseString(resources)
                            .let(::append)
                    }
                }
            }
        }
    }

    lastMessage.hasAttachments() -> {
        buildAnnotatedString {
            val attachments = lastMessage.attachments.orEmpty()

            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                if (attachments.size == 1) {
                    getAttachmentUiText(attachments.first())
                        .parseString(resources)
                        .let(::append)
                } else {
                    when {
                        isAttachmentsHaveOneType(attachments) -> {
                            getAttachmentUiText(attachments.first(), attachments.size)
                                .parseString(resources)
                                .let(::append)
                        }

                        attachments.any { it.type == AttachmentType.ARTIST } -> {
                            getAttachmentUiText(
                                attachments.first { it.type == AttachmentType.ARTIST }
                            )
                                .parseString(resources)
                                .let(::append)
                        }

                        else -> {
                            UiText.Resource(UiR.string.message_attachments_many)
                                .parseString(resources)
                                .let(::append)
                        }
                    }
                }
            }
        }
    }

    else -> null
}

private fun getAttachmentIconByType(attachmentType: AttachmentType): UiImage? {
    return when (attachmentType) {
        AttachmentType.PHOTO -> UiR.drawable.ic_attachment_photo
        AttachmentType.VIDEO -> UiR.drawable.ic_attachment_video
        AttachmentType.AUDIO -> UiR.drawable.ic_attachment_audio
        AttachmentType.FILE -> UiR.drawable.ic_attachment_file
        AttachmentType.LINK -> UiR.drawable.ic_attachment_link
        AttachmentType.AUDIO_MESSAGE -> UiR.drawable.ic_attachment_voice
        AttachmentType.MINI_APP -> UiR.drawable.ic_attachment_mini_app
        AttachmentType.STICKER -> UiR.drawable.ic_attachment_sticker
        AttachmentType.GIFT -> UiR.drawable.ic_attachment_gift
        AttachmentType.WALL -> UiR.drawable.ic_attachment_wall
        AttachmentType.GRAFFITI -> UiR.drawable.ic_attachment_graffiti
        AttachmentType.POLL -> UiR.drawable.ic_attachment_poll
        AttachmentType.WALL_REPLY -> UiR.drawable.ic_attachment_wall_reply
        AttachmentType.CALL -> UiR.drawable.ic_attachment_call
        AttachmentType.GROUP_CALL_IN_PROGRESS -> UiR.drawable.ic_attachment_group_call
        AttachmentType.STORY -> UiR.drawable.ic_attachment_story
        AttachmentType.UNKNOWN -> null
        AttachmentType.CURATOR -> null
        AttachmentType.EVENT -> null
        AttachmentType.WIDGET -> null
        AttachmentType.ARTIST -> null
        AttachmentType.AUDIO_PLAYLIST -> null
        AttachmentType.PODCAST -> null
        AttachmentType.NARRATIVE -> null
        AttachmentType.ARTICLE -> null
    }?.let(UiImage::Resource)
}

private fun isAttachmentsHaveOneType(attachments: List<VkAttachment>): Boolean {
    if (attachments.isEmpty()) return true
    if (attachments.size == 1) return true

    val firstType = attachments.first().type

    for (attachment in attachments) {
        if (firstType != attachment.type) return false
    }

    return true
}

private fun extractForwardsText(
    resources: Resources,
    lastMessage: VkMessage?
): AnnotatedString? = when {
    lastMessage == null -> null
    lastMessage.hasForwards() -> buildAnnotatedString {
        val forwards = lastMessage.forwards.orEmpty()

        withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(
                UiText.Resource(
                    if (forwards.size == 1) UiR.string.forwarded_message
                    else UiR.string.forwarded_messages
                ).parseString(resources)
            )
        }
    }

    else -> null
}


private fun getTextWithVisualizedMentions(
    originalText: String,
    mentionColor: Color,
): AnnotatedString = buildAnnotatedString {
    val regex = """\[(id|club)(\d+)\|([^]]+)]""".toRegex()

    val mentions = mutableListOf<MentionIndex>()

    var currentIndex = 0
    val replacements = mutableListOf<Pair<IntRange, String>>()

    val result = regex.replace(originalText) { matchResult ->
        val idPrefix = matchResult.groups[1]?.value.orEmpty()
        val startIndex = matchResult.range.first
        val endIndex = matchResult.range.last

        val id = matchResult.groups[2]?.value ?: ""

        val replaced = matchResult.groups[3]?.value.orEmpty()

        val indexRange =
            (startIndex + currentIndex)..startIndex + currentIndex + replaced.length

        replacements.add(indexRange to replaced)

        mentions += MentionIndex(
            id = id.toIntOrNull() ?: -1,
            idPrefix = idPrefix,
            indexRange = indexRange
        )

        currentIndex += replaced.length - (endIndex - startIndex + 1)

        replaced
    }

    append(result)

    mentions.forEach { mention ->
        val startIndex = mention.indexRange.first
        val endIndex = mention.indexRange.last

        addStyle(
            style = SpanStyle(color = mentionColor),
            start = startIndex,
            end = endIndex
        )
        addStringAnnotation(
            tag = mention.idPrefix,
            annotation = mention.id.toString(),
            start = startIndex,
            end = endIndex
        )
    }
}

data class MentionIndex(
    val id: Int,
    val idPrefix: String,
    val indexRange: IntRange
)

private fun getAttachmentUiText(
    attachment: VkAttachment,
    size: Int = 1,
): UiText {
    if (attachment.type.isMultiple()) {
        return when (attachment.type) {
            AttachmentType.PHOTO -> UiR.plurals.attachment_photos
            AttachmentType.VIDEO -> UiR.plurals.attachment_videos
            AttachmentType.AUDIO -> UiR.plurals.attachment_audios
            AttachmentType.FILE -> UiR.plurals.attachment_files
            else -> throw IllegalArgumentException("Unknown multiple type: ${attachment.type}")
        }.let { resId -> UiText.QuantityResource(resId, size) }
    }

    return when (attachment.type) {
        AttachmentType.UNKNOWN,
        AttachmentType.PHOTO,
        AttachmentType.VIDEO,
        AttachmentType.AUDIO,
        AttachmentType.FILE -> {
            throw IllegalArgumentException("Unknown multiple type: ${attachment.type}")
        }

        AttachmentType.LINK -> UiR.string.message_attachments_link
        AttachmentType.AUDIO_MESSAGE -> UiR.string.message_attachments_audio_message
        AttachmentType.MINI_APP -> UiR.string.message_attachments_mini_app
        AttachmentType.STICKER -> UiR.string.message_attachments_sticker
        AttachmentType.GIFT -> UiR.string.message_attachments_gift
        AttachmentType.WALL -> UiR.string.message_attachments_wall
        AttachmentType.GRAFFITI -> UiR.string.message_attachments_graffiti
        AttachmentType.POLL -> UiR.string.message_attachments_poll
        AttachmentType.WALL_REPLY -> UiR.string.message_attachments_wall_reply
        AttachmentType.CALL -> UiR.string.message_attachments_call
        AttachmentType.GROUP_CALL_IN_PROGRESS -> UiR.string.message_attachments_call_in_progress
        AttachmentType.CURATOR -> UiR.string.message_attachments_curator
        AttachmentType.EVENT -> UiR.string.message_attachments_event
        AttachmentType.STORY -> UiR.string.message_attachments_story
        AttachmentType.WIDGET -> UiR.string.message_attachments_widget
        AttachmentType.ARTIST -> UiR.string.message_attachments_artist
        AttachmentType.AUDIO_PLAYLIST -> UiR.string.message_attachments_audio_playlist
        AttachmentType.PODCAST -> UiR.string.message_attachments_podcast
        AttachmentType.NARRATIVE -> UiR.string.message_attachments_narrative
        AttachmentType.ARTICLE -> UiR.string.message_attachments_article
    }.let(UiText::Resource)
}

private fun getAttachmentConversationIcon(message: VkMessage?): UiImage? {
    return message?.attachments?.let { attachments ->
        if (attachments.isEmpty()) return null
        if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
            message.geoType?.let {
                return UiImage.Resource(UiR.drawable.ic_map_marker)
            }
            getAttachmentIconByType(attachments.first().type)
        } else {
            UiImage.Resource(UiR.drawable.ic_baseline_attach_file_24)
        }
    }
}

private fun extractBirthday(conversation: VkConversation): Boolean {
    val birthday = conversation.user?.birthday ?: return false
    val splitBirthday = birthday.split(".").mapNotNull(String::toIntOrNull)

    if (splitBirthday.isEmpty()) return false

    return if (splitBirthday.size > 1) {
        val (day, month) = splitBirthday

        val birthdayCalendar = Calendar.getInstance().also { calendar ->
            calendar.dayOfMonth = day
            calendar.month = month - 1
        }

        val nowCalendar = Calendar.getInstance()

        nowCalendar.dayOfMonth == birthdayCalendar.dayOfMonth &&
                nowCalendar.month == birthdayCalendar.month
    } else false
}

private fun extractReadCondition(
    conversation: VkConversation,
    lastMessage: VkMessage?
): Boolean = (lastMessage?.isOut == true && conversation.isOutUnread()) ||
        (lastMessage?.isOut == false && conversation.isInUnread())

private fun isAccount(peerId: Int) = peerId == UserConfig.userId

private fun extractInteractionText(
    resources: Resources,
    conversation: VkConversation
): String? {
    val interactionType = InteractionType.parse(conversation.interactionType)
    val interactiveUsers = extractInteractionUsers(conversation)

    val typingText =
        if (interactionType == null) {
            null
        } else {
            if (!conversation.peerType.isChat() && interactiveUsers.size == 1) {
                when (interactionType) {
                    InteractionType.File -> UiR.string.chat_interaction_uploading_file
                    InteractionType.Photo -> UiR.string.chat_interaction_uploading_photo
                    InteractionType.Typing -> UiR.string.chat_interaction_typing
                    InteractionType.Video -> UiR.string.chat_interaction_uploading_video
                    InteractionType.VoiceMessage -> UiR.string.chat_interaction_recording_audio_message
                }.let(UiText::Resource)
            } else {
                if (interactiveUsers.size == 1) {
                    UiR.string.chat_interaction_chat_single_typing
                } else {
                    UiR.string.chat_interaction_chat_typing
                }.let { resId ->
                    UiText.ResourceParams(
                        resId,
                        listOf(interactiveUsers.joinToString(separator = ", "))
                    )
                }
            }.parseString(resources)
        }

    return typingText
}

private fun extractInteractionUsers(conversation: VkConversation): List<String> {
    return conversation.interactionIds.mapNotNull { id ->
        when {
            id > 0 -> VkMemoryCache.getUser(id)?.fullName
            id < 0 -> VkMemoryCache.getGroup(id)?.name
            else -> null
        }
    }
}
