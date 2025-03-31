package dev.meloda.fast.conversations.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.DotsFlashing
import dev.meloda.fast.ui.model.api.ConversationOption
import dev.meloda.fast.ui.model.api.UiConversation
import dev.meloda.fast.ui.util.getImage
import dev.meloda.fast.ui.util.getResourcePainter
import dev.meloda.fast.ui.util.getString
import dev.meloda.fast.ui.R as UiR

val BirthdayColor = Color(0xffb00b69)

@Composable
fun ConversationItem(
    onItemClick: (Long) -> Unit,
    onItemLongClick: (conversation: UiConversation) -> Unit,
    onOptionClicked: (UiConversation, ConversationOption) -> Unit,
    maxLines: Int,
    isUserAccount: Boolean,
    conversation: UiConversation,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    val bottomStartCornerRadius by animateDpAsState(
        targetValue = if (conversation.isExpanded) 10.dp else 34.dp,
        label = "bottomStartCornerRadius"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onItemClick(conversation.id) },
                onLongClick = {
                    onItemLongClick(conversation)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
    ) {
        val showBackground by remember(conversation) {
            derivedStateOf { conversation.isUnread || conversation.isExpanded }
        }

        AnimatedVisibility(
            visible = showBackground,
            modifier = Modifier
                .matchParentSize()
                .padding(start = 8.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(
                        RoundedCornerShape(
                            topStart = 34.dp,
                            bottomStart = bottomStartCornerRadius
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.size(56.dp)) {
                    if (isUserAccount) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(32.dp),
                                painter = painterResource(id = UiR.drawable.ic_round_bookmark_border_24),
                                contentDescription = "Favorites icon",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        val avatarImage = conversation.avatar?.getImage()
                        if (avatarImage is Painter) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                painter = avatarImage,
                                contentDescription = "Avatar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            AsyncImage(
                                model = avatarImage,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                placeholder = painterResource(id = UiR.drawable.ic_account_circle_cut)
                            )
                        }
                    }

                    if (conversation.isPinned) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .height(14.dp)
                                    .align(Alignment.Center),
                                painter = painterResource(id = UiR.drawable.ic_round_push_pin_24),
                                contentDescription = "Pin icon",
                                tint = Color.White
                            )
                        }
                    }

                    if (conversation.isOnline) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(18.dp)
                                .background(
                                    if (conversation.isUnread) {
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    }
                                )
                                .padding(2.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .matchParentSize()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }

                    if (conversation.isBirthday) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(16.dp)
                                .background(
                                    if (conversation.isUnread) {
                                        MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    }
                                )
                                .padding(2.dp)
                                .align(Alignment.TopEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .matchParentSize()
                                    .background(BirthdayColor)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(10.dp),
                                    painter = painterResource(id = UiR.drawable.round_cake_24),
                                    contentDescription = "Birthday icon",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = conversation.title,
                        minLines = 1,
                        maxLines = maxLines,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
                    )

                    Row {
                        if (conversation.interactionText != null) {
                            Text(
                                text = conversation.interactionText.orEmpty(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            DotsFlashing(
                                modifier = Modifier
                                    .align(Alignment.Bottom)
                                    .padding(bottom = 7.dp),
                                dotSize = 4.dp,
                                dotColor = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            conversation.attachmentImage?.getResourcePainter()?.let { painter ->
                                Column {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Icon(
                                        modifier = Modifier.size(14.dp),
                                        painter = painter,
                                        contentDescription = "attachment image",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(2.dp))
                            }

                            LocalContentAlpha(alpha = ContentAlpha.medium) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = kotlin.run {
                                        val builder =
                                            AnnotatedString.Builder(conversation.message.text)

                                        conversation.message.spanStyles.map { spanStyleRange ->
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

                                        conversation.message.paragraphStyles.forEach { style ->
                                            builder.addStyle(
                                                style = style.item,
                                                start = style.start,
                                                end = style.end
                                            )
                                        }

                                        builder.toAnnotatedString()
                                    },
                                    minLines = 1,
                                    maxLines = maxLines,
                                    style = MaterialTheme.typography.bodyLarge,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    LocalContentAlpha(alpha = ContentAlpha.medium) {
                        Text(
                            text = conversation.date,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    conversation.unreadCount?.let { count ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                .background(MaterialTheme.colorScheme.primary)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .align(Alignment.Center),
                                text = count,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(24.dp))
            }

            AnimatedVisibility(conversation.isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        conversation.options.forEach { option ->
                            ElevatedAssistChip(
                                onClick = { onOptionClicked(conversation, option) },
                                leadingIcon = {
                                    option.icon.getResourcePainter()?.let { painter ->
                                        Icon(
                                            painter = painter,
                                            contentDescription = "Chip icon",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(text = option.title.getString().orEmpty())
                                }
                            )
                        }
                    }
                }
            }

            val bottomSpacerHeight by animateDpAsState(
                targetValue = if (conversation.isExpanded) 4.dp else 8.dp,
                label = "bottomSpacerHeight"
            )

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}


