package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

@Composable
fun OutgoingMessageBubble(
    modifier: Modifier = Modifier,
    enableAnimations: Boolean,
    message: UiItem.Message,
    onClick: (VkAttachment) -> Unit = {},
    onLongClick: (VkAttachment) -> Unit = {},
    onReplyClick: () -> Unit = {}
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentOnReplyClick by rememberUpdatedState(onReplyClick)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enableAnimations) Modifier.animateContentSize()
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            modifier = Modifier
                .padding(end = 16.dp)
                .fillMaxWidth(0.85f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            MessageBubble(
                modifier = Modifier,
                text = message.text,
                isOut = true,
                date = message.date,
                isEdited = message.isEdited,
                isRead = message.isRead,
                sendingStatus = message.sendingStatus,
                isPinned = message.isPinned,
                isImportant = message.isImportant,
                isSelected = message.isSelected,
                attachments = message.attachments?.toImmutableList(),
                replyTitle = message.replyTitle,
                replySummary = message.replySummary,
                onClick = currentOnClick,
                onLongClick = currentOnLongClick,
                onReplyClick = currentOnReplyClick
            )
        }
    }
}
