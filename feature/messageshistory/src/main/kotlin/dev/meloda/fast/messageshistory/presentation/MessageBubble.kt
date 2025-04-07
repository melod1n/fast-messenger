package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.meloda.fast.messageshistory.model.SendingStatus
import dev.meloda.fast.messageshistory.presentation.attachments.Attachments
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList
import dev.meloda.fast.ui.R as UiR

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

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column {
            Box(
                modifier = modifier
                    .widthIn(min = 56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(backgroundColor)
                    .padding(
                        horizontal = 8.dp,
                        vertical = 6.dp
                    )
                    .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier),
            ) {
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

                MessageTextContainer(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(2.dp)
                        .padding(end = 4.dp)
                        .padding(end = dateContainerWidth)
                        .padding(end = 4.dp)
                        .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier),
                    text = text,
                    isOut = isOut,
                    isSelected = isSelected,
                )

                Row(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .align(Alignment.BottomEnd)
                        .defaultMinSize(minWidth = dateContainerWidth)
                        .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier)
                ) {
                    if (isImportant) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(UiR.drawable.round_star_24),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )

                    }
                    if (isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(UiR.drawable.ic_round_push_pin_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(45f)
                        )
                    }
                    if (isEdited) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Create,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    if (isOut) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            painter = painterResource(
                                when (sendingStatus) {
                                    SendingStatus.SENDING -> UiR.drawable.round_access_time_24
                                    SendingStatus.SENT -> {
                                        if (isRead) UiR.drawable.round_done_all_24
                                        else UiR.drawable.ic_round_done_24
                                    }

                                    SendingStatus.FAILED -> UiR.drawable.round_error_outline_24
                                }
                            ),
                            tint = if (sendingStatus == SendingStatus.FAILED) MaterialTheme.colorScheme.error
                            else LocalContentColor.current,
                            contentDescription = null
                        )
                    }
                }
            }

            attachments?.let {
                Attachments(
                    modifier = modifier,
                    attachments = attachments
                )
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
