package com.meloda.fast.screens.conversations.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.common.collect.ImmutableList
import com.meloda.fast.R
import com.meloda.fast.ext.LocalContentAlpha
import com.meloda.fast.ext.getString
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.screens.conversations.DotsFlashing
import com.meloda.fast.screens.conversations.model.ConversationOption
import com.meloda.fast.ui.ContentAlpha

val BirthdayColor = Color(0xffb00b69)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Conversation(
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    isUserAccount: Boolean,
    avatar: UiImage,
    title: String,
    message: String,
    date: String,
    maxLines: Int,
    isUnread: Boolean,
    isPinned: Boolean,
    isOnline: Boolean,
    isBirthday: Boolean,
    interactionText: String?,
    attachmentImage: UiImage?,
    isExpanded: Boolean,
    unreadCount: String?,
    showOnlyPlaceholders: Boolean,
    modifier: Modifier,
    options: ImmutableList<ConversationOption>,
    onOptionClicked: (ConversationOption) -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val bottomStartCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 10.dp else 34.dp, label = "bottomStartCornerRadius"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = {
                    onItemLongClick()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
    ) {
        AnimatedVisibility(
            visible = isUnread || isExpanded,
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
                            topStart = 34.dp, bottomStart = bottomStartCornerRadius
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
                    if (showOnlyPlaceholders) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_account_circle_cut),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        if (isUserAccount) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            ) {
                                Image(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(32.dp),
                                    painter = painterResource(id = R.drawable.ic_round_bookmark_border_24),
                                    contentDescription = null
                                )
                            }
                        } else {
                            val avatarImage = avatar.getImage()
                            if (avatarImage is Painter) {
                                Image(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    painter = avatarImage,
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(avatarImage)
                                        .crossfade(true).build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    placeholder = painterResource(id = R.drawable.ic_account_circle_cut)
                                )
                            }
                        }
                    }

                    if (isPinned) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        ) {
                            Image(
                                modifier = Modifier
                                    .height(14.dp)
                                    .align(Alignment.Center),
                                painter = painterResource(id = R.drawable.ic_round_push_pin_24),
                                contentDescription = null
                            )
                        }
                    }

                    if (isOnline) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(18.dp)
                                .background(
                                    if (isUnread) {
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

                    if (isBirthday) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(16.dp)
                                .background(
                                    if (isUnread) {
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
                                Image(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(10.dp),
                                    painter = painterResource(id = R.drawable.round_cake_24),
                                    contentDescription = null
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
                        text = title,
                        modifier = Modifier,
                        minLines = 1,
                        maxLines = maxLines,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
                    )

                    Row {
                        if (interactionText != null) {
                            Text(
                                text = interactionText, color = MaterialTheme.colorScheme.primary
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
                            attachmentImage?.getResourcePainter()?.let { painter ->
                                Column {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Image(
                                        modifier = Modifier.size(14.dp),
                                        painter = painter,
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                    )
                                }

                                Spacer(modifier = Modifier.width(2.dp))
                            }

                            LocalContentAlpha(alpha = ContentAlpha.medium) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = message,
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
                            text = date, style = MaterialTheme.typography.bodySmall
                        )
                    }

                    unreadCount?.let { count ->
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

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()

                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        options.forEach { option ->
                            ElevatedAssistChip(
                                onClick = {
                                    onOptionClicked(option)
                                },
                                leadingIcon = {
                                    option.icon.getResourcePainter()?.let { painter ->
                                        Icon(
                                            painter = painter,
                                            contentDescription = null,
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
                targetValue = if (isExpanded) 4.dp else 8.dp,
                label = "bottomSpacerHeight"
            )

            Spacer(modifier = Modifier.height(bottomSpacerHeight))
        }
    }
}


