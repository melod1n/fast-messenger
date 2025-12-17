package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.domain.util.annotated
import dev.meloda.fast.messageshistory.presentation.attachments.Attachments
import dev.meloda.fast.messageshistory.presentation.attachments.Reply
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkStickerDomain
import dev.meloda.fast.model.api.domain.VkVideoMessageDomain
import dev.meloda.fast.ui.model.vk.SendingStatus
import dev.meloda.fast.ui.theme.AppTheme
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.darken
import dev.meloda.fast.ui.util.emptyImmutableList
import dev.meloda.fast.ui.util.isDark
import dev.meloda.fast.ui.util.lighten

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    text: AnnotatedString?,
    isOut: Boolean,
    date: String,
    isEdited: Boolean,
    isRead: Boolean,
    sendingStatus: SendingStatus,
    isPinned: Boolean,
    isImportant: Boolean,
    isSelected: Boolean,
    attachments: ImmutableList<VkAttachment>?,
    replyTitle: String?,
    replySummary: AnnotatedString? = null,
    onClick: (VkAttachment) -> Unit = {},
    onLongClick: (VkAttachment) -> Unit = {},
    onReplyClick: () -> Unit = {},
    onBubbleWidthChange: (Int) -> Unit = {},
) {
    val density = LocalDensity.current

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)

    val theme = LocalThemeConfig.current
    val colors = messageBubbleColors(isOut = isOut)

    val shouldShowBubble = !text.isNullOrEmpty()

    var bubbleContainerWidth by remember {
        mutableIntStateOf(0)
    }

    var attachmentsContainerWidth by remember {
        mutableIntStateOf(0)
    }

    val shouldFill by derivedStateOf {
        attachmentsContainerWidth >= bubbleContainerWidth
    }

    var containerWidth by remember {
        mutableIntStateOf(0)
    }

    val minDateContainerWidth = remember(isEdited, isOut, isPinned, isImportant) {
        val mainPart = if (isEdited) 50 else 30
        val readIndicatorPart = if (isOut) 14 else 0
        val pinnedIndicatorPart = if (isPinned) 14 else 0
        val importantIndicatorPart = if (isImportant) 14 else 0

        (mainPart + readIndicatorPart + pinnedIndicatorPart + importantIndicatorPart).dp
    }

    val dateContainerWidth by animateDpAsState(
        targetValue = minDateContainerWidth,
        label = "dateContainerWidth"
    )

    CompositionLocalProvider(LocalContentColor provides colors.content) {
        Column(
            modifier = modifier
                .wrapContentWidth()
                .onGloballyPositioned {
                    containerWidth = it.size.width
                }
        ) {
            if (replyTitle != null) {
                Reply(
                    modifier = Modifier
                        .padding(if (attachments == null || text != null) 0.dp else 4.dp)
                        .width(with(density) { containerWidth.toDp() }),
                    bottomPadding = if (attachments == null || text != null) 0.dp else 4.dp,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (attachments == null || text != null) 0.dp else 16.dp,
                        bottomEnd = if (attachments == null || text != null) 0.dp else 16.dp
                    ),
                    onClick = onReplyClick,
                    title = replyTitle,
                    summary = replySummary,
                    backgroundColor = colors.container,
                    innerBackgroundColor = colors.replyContainer
                )
            }

            if (shouldShowBubble) {
                Box(
                    modifier = modifier
                        .onGloballyPositioned {
                            bubbleContainerWidth = it.size.width
                        }
                        .onSizeChanged {
                            onBubbleWidthChange(it.width)
                        }
                        .widthIn(min = if (shouldFill) attachmentsContainerWidth.dp else 56.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = if (replyTitle == null) 24.dp else 0.dp,
                                topEnd = if (replyTitle == null) 24.dp else 0.dp,
                                bottomStart = if (attachments != null) 0.dp else 24.dp,
                                bottomEnd = if (attachments != null) 0.dp else 24.dp
                            )
                        )
                        .background(colors.container)
                        .padding(
                            start = 8.dp,
                            end = 8.dp,
                            top = if (replyTitle != null) 0.dp else 6.dp,
                            bottom = if (replyTitle != null) 4.dp else 6.dp
                        )
                        .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier),
                ) {
                    MessageTextContainer(
                        modifier = Modifier
                            .padding(2.dp)
                            .padding(end = 4.dp)
                            .padding(end = dateContainerWidth)
                            .padding(end = 4.dp)
                            .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier),
                        text = text,
                        isOut = isOut,
                        isSelected = isSelected,
                    )

                    if (attachments == null) {
                        DateStatus(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .align(Alignment.BottomEnd)
                                .defaultMinSize(minWidth = dateContainerWidth),
                            dateContainerWidth = dateContainerWidth,
                            date = date,
                            sendingStatus = sendingStatus,
                            isImportant = isImportant,
                            isPinned = isPinned,
                            isEdited = isEdited,
                            isOut = isOut,
                            isRead = isRead
                        )
                    }
                }
            }

            if (attachments != null) {
                val firstAttachment = attachments.firstOrNull()
                val isMediaAttachment = firstAttachment is VkStickerDomain ||
                        firstAttachment is VkVideoMessageDomain
                val attachmentBackgroundColor = if (isMediaAttachment) {
                    Color.Transparent
                } else {
                    colors.container
                }

                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            attachmentsContainerWidth = it.size.width
                        }
                        .clip(
                            if (!shouldShowBubble) RoundedCornerShape(24.dp)
                            else RoundedCornerShape(
                                bottomEnd = 24.dp,
                                bottomStart = 24.dp,
                                topStart = 0.dp,
                                topEnd = 0.dp
                            )
                        )
                        .background(attachmentBackgroundColor)
                ) {
                    Attachments(
                        modifier = Modifier,
                        attachments = attachments,
                        onClick = currentOnClick,
                        onLongClick = currentOnLongClick
                    )

                    val dateStatusBackground = if (theme.darkMode) Color.Black.copy(alpha = 0.5f)
                    else Color.White.copy(alpha = 0.5f)

                    CompositionLocalProvider(LocalContentColor provides colors.content) {
                        DateStatus(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 6.dp, end = 6.dp)
                                .widthIn(min = 42.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(dateStatusBackground)
                                .padding(4.dp),
                            dateContainerWidth = dateContainerWidth,
                            date = date,
                            sendingStatus = sendingStatus,
                            isImportant = isImportant,
                            isPinned = isPinned,
                            isEdited = isEdited,
                            isOut = isOut,
                            isRead = isRead
                        )
                    }
                }
            }
        }
    }
}

@Immutable
private data class MessageBubbleColors(
    val container: Color,
    val content: Color,
    val replyContainer: Color,
)

@Composable
private fun messageBubbleColors(isOut: Boolean): MessageBubbleColors {
    return if (isOut) {
        val containerColor = MaterialTheme.colorScheme.primaryContainer

        val replyContainerColor = if (containerColor.isDark()) {
            containerColor.lighten(0.15f)
        } else {
            containerColor.darken(0.075f)
        }

        MessageBubbleColors(
            container = containerColor,
            content = MaterialTheme.colorScheme.onPrimaryContainer,
            replyContainer = replyContainerColor
        )
    } else {
        MessageBubbleColors(
            container = MaterialTheme.colorScheme.surfaceContainer,
            content = MaterialTheme.colorScheme.onSurface,
            replyContainer = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Preview
@Composable
private fun Bubble() {
    AppTheme(
        useDarkTheme = true,
        useDynamicColors = true
    ) {
        Column {
            MessageBubble(
                modifier = Modifier,
                text = AnnotatedString("Some cool text"),
                isOut = true,
                date = "19:01",
                isEdited = true,
                isRead = true,
                sendingStatus = SendingStatus.SENT,
                isPinned = true,
                isImportant = true,
                isSelected = false,
                attachments = emptyImmutableList(),
                replyTitle = "Danil Nikolaev",
                replySummary = "2 photos".annotated(),
                onClick = {},
                onLongClick = {},
            )

            MessageBubble(
                modifier = Modifier,
                text = AnnotatedString("Some cool text"),
                isOut = false,
                date = "19:01",
                isEdited = true,
                isRead = true,
                sendingStatus = SendingStatus.SENT,
                isPinned = true,
                isImportant = true,
                isSelected = false,
                attachments = emptyImmutableList(),
                replyTitle = "Danil Nikolaev",
                replySummary = "2 photos".annotated(),
                onClick = {},
                onLongClick = {},
            )
        }
    }
}
