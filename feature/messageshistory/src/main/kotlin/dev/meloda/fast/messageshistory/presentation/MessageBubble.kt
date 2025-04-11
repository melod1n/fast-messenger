package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.messageshistory.model.SendingStatus
import dev.meloda.fast.messageshistory.presentation.attachments.Attachments
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList

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
) {
    val theme = LocalThemeConfig.current
    val backgroundColor = if (!isOut) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = if (!isOut) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val minDateContainerWidth by remember(isEdited, isOut, isPinned, isImportant) {
        derivedStateOf {
            val mainPart = if (isEdited) 50.dp else 30.dp
            val readIndicatorPart = if (isOut) 14.dp else 0.dp
            val pinnedIndicatorPart = if (isPinned) 14.dp else 0.dp
            val importantIndicatorPart = if (isImportant) 14.dp else 0.dp

            mainPart + readIndicatorPart + pinnedIndicatorPart + importantIndicatorPart
        }
    }

    val dateContainerWidth by animateDpAsState(
        targetValue = minDateContainerWidth,
        label = "dateContainerWidth"
    )

    val shouldShowBubble by remember(text) {
        derivedStateOf { text != null }
    }

    var bubbleContainerWidth by remember {
        mutableIntStateOf(0)
    }

    var attachmentsContainerWidth by remember {
        mutableIntStateOf(0)
    }

    val shouldFill by remember(bubbleContainerWidth, attachmentsContainerWidth) {
        derivedStateOf {
            attachmentsContainerWidth >= bubbleContainerWidth
        }
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column {
            if (shouldShowBubble) {
                Box(
                    modifier = modifier
                        .onGloballyPositioned {
                            bubbleContainerWidth = it.size.width
                        }
                        .widthIn(min = if (shouldFill) attachmentsContainerWidth.dp else 56.dp)
                        .clip(
                            if (attachments == null) RoundedCornerShape(24.dp)
                            else RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                        .background(backgroundColor)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 6.dp
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
                        .background(backgroundColor)
                ) {
                    Attachments(
                        modifier = Modifier,
                        attachments = attachments
                    )

                    val dateStatusBackground = if (theme.darkMode) Color.Black.copy(alpha = 0.5f)
                    else Color.White.copy(alpha = 0.5f)

                    CompositionLocalProvider(LocalContentColor provides contentColor) {
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

@Preview
@Composable
private fun Bubble() {
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
        attachments = emptyImmutableList()
    )
}
