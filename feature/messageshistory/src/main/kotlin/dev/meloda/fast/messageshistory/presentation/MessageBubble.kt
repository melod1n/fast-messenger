package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import dev.meloda.fast.messageshistory.model.SendingStatus
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.R as UiR

@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    text: AnnotatedString?,
    isOut: Boolean,
    date: String?,
    edited: Boolean,
    isRead: Boolean,
    sendingStatus: SendingStatus,
    pinned: Boolean,
    important: Boolean,
    isSelected: Boolean
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
            val minDateContainerWidth by remember(edited, isOut, pinned, important) {
                derivedStateOf {
                    val mainPart = if (edited) 50.dp else 30.dp
                    val readIndicatorPart = if (isOut) 14.dp else 0.dp
                    val pinnedIndicatorPart = if (pinned) 14.dp else 0.dp
                    val importantIndicatorPart = if (important) 14.dp else 0.dp

                    mainPart + readIndicatorPart + pinnedIndicatorPart + importantIndicatorPart
                }
            }

            val dateContainerWidth by animateDpAsState(
                targetValue = minDateContainerWidth,
                label = "dateContainerWidth"
            )

            if (text != null) {
                val textLambda: @Composable () -> Unit = remember(text, theme, dateContainerWidth) {
                    {
                        Text(
                            text = kotlin.run {
                                val builder = AnnotatedString.Builder(text)

                                text.spanStyles.map { spanStyleRange ->
                                    val updatedSpanStyle =
                                        if (spanStyleRange.item.color == Color.Red) {
                                            spanStyleRange.item.copy(color = MaterialTheme.colorScheme.primary)
                                        } else {
                                            spanStyleRange.item
                                        }

                                    builder.addStyle(
                                        style = updatedSpanStyle,
                                        start = spanStyleRange.start,
                                        end = spanStyleRange.end
                                    )
                                }

                                text.paragraphStyles.forEach { style ->
                                    builder.addStyle(
                                        style = style.item,
                                        start = style.start,
                                        end = style.end
                                    )
                                }

                                builder.toAnnotatedString()
                            },
                            modifier = Modifier
                                .padding(2.dp)
                                .align(Alignment.Center)
                                .padding(end = 4.dp)
                                .padding(end = dateContainerWidth)
                                .padding(end = 4.dp)
                                .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier)
                        )
                    }
                }

                if (isSelected) {
                    SelectionContainer {
                        textLambda.invoke()
                    }
                } else {
                    textLambda.invoke()
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .defaultMinSize(minWidth = dateContainerWidth)
                    .then(if (theme.enableAnimations) Modifier.animateContentSize() else Modifier),
            ) {
                if (important) {
                    Icon(
                        painter = painterResource(UiR.drawable.round_star_24),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (pinned) {
                    Icon(
                        painter = painterResource(UiR.drawable.ic_round_push_pin_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(45f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (edited) {
                    Icon(
                        imageVector = Icons.Rounded.Create,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = date.orEmpty(),
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
                        tint = if (sendingStatus == SendingStatus.FAILED) Color.Red
                        else LocalContentColor.current,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
