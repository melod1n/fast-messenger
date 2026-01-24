package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.conena.nanokt.android.content.dpInPx
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.vk.MessageUiItem
import kotlin.math.roundToInt

@Composable
fun OutgoingMessageBubble(
    modifier: Modifier = Modifier,
    enableAnimations: Boolean,
    message: MessageUiItem.Message,
    offsetX: Float = 0f,
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
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reply_round_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset { IntOffset(24.dpInPx + offsetX.roundToInt(), y = 0) }
            )

            Row(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
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
                    attachments = message.attachments,
                    replyTitle = message.replyTitle,
                    replySummary = message.replySummary,
                    onClick = currentOnClick,
                    onLongClick = currentOnLongClick,
                    onReplyClick = currentOnReplyClick
                )
            }
        }
    }
}
