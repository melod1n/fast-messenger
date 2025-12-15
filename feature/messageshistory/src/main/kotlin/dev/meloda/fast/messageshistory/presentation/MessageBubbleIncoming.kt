package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import com.conena.nanokt.android.content.dpInPx
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.ui.R
import kotlin.math.roundToInt

@Composable
fun IncomingMessageBubble(
    enableAnimations: Boolean,
    modifier: Modifier = Modifier,
    message: UiItem.Message,
    offsetX: Float = 0f,
    onClick: (VkAttachment) -> Unit = {},
    onLongClick: (VkAttachment) -> Unit = {},
    onReplyClick: () -> Unit = {}
) {
    val density = LocalDensity.current

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentOnReplyClick by rememberUpdatedState(onReplyClick)

    var bubbleContainerWidth by remember {
        mutableStateOf(0.dp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enableAnimations) Modifier.animateContentSize()
                else Modifier
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(R.drawable.round_reply_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset { IntOffset(24.dpInPx + offsetX.roundToInt(), y = 0) }
            )

            Row(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .fillMaxWidth(0.85f)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                if (message.isInChat) {
                    Image(
                        painter =
                            message.avatar.extractUrl()?.let { url ->
                                rememberAsyncImagePainter(
                                    model = url,
                                    imageLoader = LocalContext.current.imageLoader
                                )
                            } ?: painterResource(id = message.avatar.extractResId()),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .size(28.dp)
                            .alpha(if (message.showAvatar) 1f else 0f)
                            .clip(CircleShape),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column {
                    AnimatedVisibility(visible = message.showName) {
                        Text(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .widthIn(
                                    max = (bubbleContainerWidth.takeIf { it > 0.dp }
                                        ?: 140.dp) - 24.dp
                                ),
                            text = message.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    MessageBubble(
                        modifier = Modifier,
                        text = message.text,
                        isOut = false,
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
                        onReplyClick = currentOnReplyClick,
                        onBubbleWidthChange = {
                            bubbleContainerWidth = with(density) { it.toDp() }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.fillMaxWidth(0.25f))
        }
    }
}
