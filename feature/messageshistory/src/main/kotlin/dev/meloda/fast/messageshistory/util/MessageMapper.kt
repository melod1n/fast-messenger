package dev.meloda.fast.messageshistory.util

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Annotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.StringAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.common.model.UiText
import dev.meloda.fast.common.model.parseString
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.messageshistory.model.SendingStatus
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.api.domain.FormatDataType
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.R
import java.text.SimpleDateFormat
import java.util.Locale
import dev.meloda.fast.ui.R as UiR

private fun isAccount(fromId: Long) = fromId == UserConfig.userId

fun VkMessage.extractAvatar() = when {
    isUser() -> {
        if (isAccount(id)) null
        else user?.photo200
    }

    isGroup() -> {
        group?.photo200
    }

    else -> null
}?.let(UiImage::Url) ?: UiImage.Resource(UiR.drawable.ic_account_circle_cut)

fun VkMessage.extractDate(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date * 1000L)

fun VkMessage.extractTitle(): String = when {
    isUser() -> "%s %s".format(
        user?.firstName.orDots(),
        user?.lastName?.firstOrNull()?.toString().orEmpty().plus(".")
    )

    isGroup() -> group?.name.orDots()

    else -> throw IllegalStateException("Message is not from user nor group. fromId: $fromId")
}

fun VkMessage.extractReplyTitle(): String? = replyMessage?.extractTitle()

// TODO: 24-Jun-25, Danil Nikolaev: improve
fun VkMessage.extractReplySummary(): String? = when (val message = replyMessage) {
    null -> null
    else -> {
        when {
            message.text != null -> message.text
            else -> null
        }
    }
}

fun VkConversation.extractAvatar(): UiImage = when (peerType) {
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
}?.let(UiImage::Url) ?: UiImage.Resource(R.drawable.ic_account_circle_cut)

fun VkConversation.extractTitle(
    useContactName: Boolean,
    resources: Resources
) = when (peerType) {
    PeerType.USER -> {
        if (isAccount(id)) {
            UiText.Resource(UiR.string.favorites)
        } else {
            val userName = user?.let { user ->
                if (useContactName) {
                    VkMemoryCache.getContact(user.id)?.name
                } else {
                    user.fullName
                }
            }

            UiText.Simple(userName.orDots())
        }
    }

    PeerType.GROUP -> UiText.Simple(group?.name.orDots())
    PeerType.CHAT -> UiText.Simple(title.orDots())
}.parseString(resources).orDots()

fun VkMessage.asPresentation(
    conversation: VkConversation,
    resourceProvider: ResourceProvider,
    showName: Boolean,
    prevMessage: VkMessage?,
    nextMessage: VkMessage?,
    showTimeInActionMessages: Boolean,
    isSelected: Boolean
): UiItem = when {
    action != null -> UiItem.ActionMessage(
        id = id,
        cmId = cmId,
        text = extractActionText(
            resources = resourceProvider.resources,
            youPrefix = resourceProvider.getString(R.string.you_message_prefix),
            showTime = showTimeInActionMessages
        ) ?: buildAnnotatedString { },
        actionCmId = actionConversationMessageId
    )

    else -> UiItem.Message(
        id = id,
        cmId = cmId,
        text = extractTextWithVisualizedMentions(
            isOut = isOut,
            originalText = text,
            formatData = formatData
        ),
        isOut = isOut,
        fromId = fromId,
        date = extractDate(),
        randomId = randomId,
        isInChat = isPeerChat(),
        name = extractTitle(),
        showDate = true,
        showAvatar = extractShowAvatar(nextMessage),
        showName = showName && extractShowName(prevMessage),
        avatar = extractAvatar(),
        isEdited = updateTime != null,
        isRead = isRead(conversation),
        sendingStatus = when {
            isFailed() -> SendingStatus.FAILED
            id <= 0 -> SendingStatus.SENDING
            else -> SendingStatus.SENT
        },
        isSelected = isSelected,
        isPinned = isPinned,
        isImportant = isImportant,
        attachments = attachments?.ifEmpty { null },
        replyCmId = replyMessage?.cmId,
        replyTitle = extractReplyTitle(),
        replySummary = extractReplySummary()
    )
}

fun VkMessage.extractShowAvatar(nextMessage: VkMessage?): Boolean {
    if (isOut) return false
    return nextMessage == null || nextMessage.fromId != fromId
}

fun VkMessage.extractShowName(prevMessage: VkMessage?): Boolean {
    if (isOut || !isPeerChat()) return false
    return prevMessage == null || prevMessage.fromId != fromId
}

fun VkMessage.extractActionText(
    resources: Resources,
    youPrefix: String,
    showTime: Boolean
): AnnotatedString? {
    val lastMessage = this

    val action = lastMessage.action ?: return null

    val formattedMessageDate =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(lastMessage.date * 1000L)

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
        when (action) {
            VkMessage.Action.CHAT_CREATE -> {
                val string = UiText.ResourceParams(
                    UiR.string.message_action_chat_created,
                    listOf(prefix, text)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )

                val textStartIndex = string.indexOf(text)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = textStartIndex,
                    end = textStartIndex + text.length
                )
            }

            VkMessage.Action.CHAT_TITLE_UPDATE -> {
                val string = UiText.ResourceParams(
                    UiR.string.message_action_chat_renamed,
                    listOf(prefix, text)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )

                val textStartIndex = string.indexOf(text)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = textStartIndex,
                    end = textStartIndex + text.length
                )
            }

            VkMessage.Action.CHAT_PHOTO_UPDATE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_photo_update,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_PHOTO_REMOVE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_photo_remove,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_KICK_USER -> {
                if (memberId == fromId) {
                    UiText.ResourceParams(
                        UiR.string.message_action_chat_user_left,
                        listOf(memberPrefix)
                    ).parseString(resources)
                        .orEmpty()
                        .let { text ->
                            if (showTime) {
                                text.plus("\n")
                                    .plus(formattedMessageDate)
                            } else text
                        }.also(::append)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Medium),
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
                    ).parseString(resources)
                        .orEmpty()
                        .let { text ->
                            if (showTime) {
                                text.plus("\n")
                                    .plus(formattedMessageDate)
                            } else text
                        }.also(::append)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Medium),
                        start = 0,
                        end = prefix.length
                    )

                    val postfixStartIndex = string.indexOf(postfix)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Medium),
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
                    ).parseString(resources)
                        .orEmpty()
                        .let { text ->
                            if (showTime) {
                                text.plus("\n")
                                    .plus(formattedMessageDate)
                            } else text
                        }.also(::append)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Medium),
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
                    ).parseString(resources)
                        .orEmpty()
                        .let { text ->
                            if (showTime) {
                                text.plus("\n")
                                    .plus(formattedMessageDate)
                            } else text
                        }.also(::append)

                    val postfixStartIndex = string.indexOf(postfix)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Medium),
                        start = postfixStartIndex,
                        end = postfixStartIndex + postfix.length
                    )
                }
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_LINK -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_user_joined_by_link,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_user_joined_by_call,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_user_joined_by_call_link,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_PIN_MESSAGE -> {
                // TODO: 16/07/2024, Danil Nikolaev: get pinned message by cmid
//                val messageText = lastMessage.text.orEmpty().trim()
//                val croppedMessage = messageText.take(40)
//                val hasMessageText = messageText.isNotEmpty()

                UiText.ResourceParams(
                    UiR.string.message_action_chat_pin_message,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
//                    .let { text ->
//                        if (hasMessageText) {
//                            text.plus("«%s»".format(croppedMessage))
//                                .plus(if (messageText.length > 40) "..." else "")
//                        } else text
//                    }
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )

//                if (hasMessageText) {
//                    val croppedIndex = fullText.indexOf(croppedMessage)
//
//                    addStyle(
//                        style = SpanStyle(fontWeight = FontWeight.Medium),
//                        start = croppedIndex - 1,
//                        end = croppedIndex - 1 + croppedMessage.length + 1
//                    )
//                }
            }

            VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_unpin_message,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_SCREENSHOT -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_screenshot,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }

            VkMessage.Action.CHAT_STYLE_UPDATE -> {
                UiText.ResourceParams(
                    UiR.string.message_action_chat_style_update,
                    listOf(prefix)
                ).parseString(resources)
                    .orEmpty()
                    .let { text ->
                        if (showTime) {
                            text.plus("\n")
                                .plus(formattedMessageDate)
                        } else text
                    }.also(::append)

                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Medium),
                    start = 0,
                    end = prefix.length
                )
            }
        }
    }
}

// TODO: 04-Apr-25, Danil Nikolaev: get rid of method duplication
fun extractTextWithVisualizedMentions(
    isOut: Boolean,
    originalText: String?,
    formatData: VkMessage.FormatData?
): AnnotatedString? {
    if (originalText == null) return null

    val annotations =
        mutableListOf<AnnotatedString.Range<out Annotation>>()

    val regex = """\[(id|club)(\d+)\|([^]]+)]""".toRegex()

    val mentions = mutableListOf<MentionIndex>()

    var currentIndex = 0
    val replacements = mutableListOf<Pair<IntRange, String>>()

    val newText = regex.replace(originalText) { matchResult ->
        val idPrefix = matchResult.groups[1]?.value.orEmpty()
        val startIndex = matchResult.range.first
        val endIndex = matchResult.range.last

        val id = matchResult.groups[2]?.value ?: ""

        val replaced = matchResult.groups[3]?.value.orEmpty()

        val indexRange =
            (startIndex + currentIndex)..startIndex + currentIndex + replaced.length

        replacements.add(indexRange to replaced)

        mentions += MentionIndex(
            id = id.toLongOrNull() ?: -1,
            idPrefix = idPrefix,
            indexRange = indexRange
        )

        currentIndex += replaced.length - (endIndex - startIndex + 1)

        replaced
    }

    mentions.forEach { mention ->
        val startIndex = mention.indexRange.first
        val endIndex = mention.indexRange.last

        annotations += if (isOut) {
            AnnotatedString.Range(
                item = SpanStyle(textDecoration = TextDecoration.Underline),
                start = startIndex,
                end = endIndex
            )
        } else {
            AnnotatedString.Range(
                item = SpanStyle(color = Color.Red),
                start = startIndex,
                end = endIndex
            )
        }

        annotations += AnnotatedString.Range(
            item = StringAnnotation(mention.id.toString()),
            tag = mention.idPrefix,
            start = startIndex,
            end = endIndex
        )
    }

    if (formatData == null) return AnnotatedString(text = newText, annotations = annotations)

    var current = 0

    val newOffsets = formatData.items.map { (offset, length) ->
        val r = replacements.filter { (range, _) ->
            (range - current) collidesWith (offset..<offset + length) || offset > range.first
        }

        current = r.sumOf { (range, _) -> range.last - range.first - 1 }

        offset + current
    }

    formatData.items.forEachIndexed { index, item ->
        val offset = newOffsets[index]

        val spanStyle = when (item.type) {
            FormatDataType.BOLD -> {
                SpanStyle(fontWeight = FontWeight.SemiBold)
            }

            FormatDataType.ITALIC -> {
                SpanStyle(fontStyle = FontStyle.Italic)
            }

            FormatDataType.UNDERLINE -> {
                SpanStyle(textDecoration = TextDecoration.Underline)
            }

            FormatDataType.URL -> {
                annotations += AnnotatedString.Range(
                    item = StringAnnotation(item.url.orEmpty()),
                    start = offset,
                    end = offset + item.length,
                    tag = newText.substring(offset, offset + item.length)
                )

                if (isOut) {
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )

                } else {
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Red
                    )
                }
            }
        }

        annotations += AnnotatedString.Range(
            item = spanStyle,
            start = offset,
            end = offset + item.length
        )
    }

    return AnnotatedString(text = newText, annotations = annotations)
}

data class MentionIndex(
    val id: Long,
    val idPrefix: String,
    val indexRange: IntRange
)

infix fun ClosedRange<Int>.collidesWith(other: ClosedRange<Int>): Boolean {
    return this.start < other.endInclusive && other.start < this.endInclusive
}

operator fun ClosedRange<Int>.minus(other: ClosedRange<Int>): ClosedRange<Int> {
    return (this.start - other.start)..(this.endInclusive - other.endInclusive)
}

operator fun ClosedRange<Int>.minus(other: Int): ClosedRange<Int> {
    return (this.start - other)..(this.endInclusive - other)
}
