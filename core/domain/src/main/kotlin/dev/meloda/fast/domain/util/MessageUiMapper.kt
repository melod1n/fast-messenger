package dev.meloda.fast.domain.util

import androidx.compose.ui.text.buildAnnotatedString
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.vk.MessageUiItem
import dev.meloda.fast.ui.model.vk.SendingStatus
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

fun VkMessage.asPresentation(
    convo: VkConvo,
    resourceProvider: ResourceProvider,
    showName: Boolean,
    prevMessage: VkMessage?,
    nextMessage: VkMessage?,
    showTimeInActionMessages: Boolean,
    isSelected: Boolean
): MessageUiItem = when {
    action != null -> MessageUiItem.ActionMessage(
        id = id,
        cmId = cmId,
        text = extractActionText(
            resources = resourceProvider.resources,
            youPrefix = resourceProvider.getString(R.string.you_message_prefix),
            showTime = showTimeInActionMessages
        ) ?: buildAnnotatedString { },
        actionCmId = actionCmId
    )

    else -> MessageUiItem.Message(
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
        isRead = isRead(convo),
        sendingStatus = when {
            isFailed() -> SendingStatus.FAILED
            id <= 0 -> SendingStatus.SENDING
            else -> SendingStatus.SENT
        },
        isSelected = isSelected,
        isPinned = isPinned,
        isImportant = isImportant,
        attachments = attachments?.ifEmpty { null }?.toImmutableList(),
        replyCmId = replyMessage?.cmId,
        replyTitle = extractReplyTitle(),
        replySummary = replyMessage?.extractReplySummary(resourceProvider.resources)
    )
}
