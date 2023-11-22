package com.meloda.fast.screens.conversations.presentation

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
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
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.ConversationPeerType
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.ext.LocalContentAlpha
import com.meloda.fast.ext.combinedClickableSound
import com.meloda.fast.screens.conversations.DotsFlashing
import com.meloda.fast.ui.ContentAlpha
import com.meloda.fast.ui.widgets.CoilImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Conversation(
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    id: Int,
    avatar: Any?,
    title: String,
    message: String,
    date: String,
    maxLines: Int,
    isUnread: Boolean,
    isPinned: Boolean,
    isOnline: Boolean,
    isBirthday: Boolean,
    interactionType: InteractionType?,
    interactiveUsers: List<String>,
    peerType: ConversationPeerType,
    attachmentImage: Drawable?,
    unreadCount: String?,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

//    val avatar = conversation.avatar.getImage()

//    val title = remember { conversation.title.orDots() }
//    val message = remember { conversation.message }
//    val date = remember { conversation.date }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickableSound(
                onClick = onItemClick,
                onLongClick = {
                    onItemLongClick()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
    ) {
        if (isUnread) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 8.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 34.dp,
                            bottomStart = 34.dp
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
                    if (id == UserConfig.userId) {
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
                        if (avatar is Painter) {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                painter = avatar,
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                            )
                        } else {
                            CoilImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentDescription = null,
                                model = ImageRequest.Builder(context)
                                    .data(avatar)
                                    .crossfade(true)
                                    .build(),
                                previewPainter = painterResource(id = R.drawable.ic_account_circle_cut),
                            )
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
                                    .background(Color(0xFFB00B69))
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
                        if (interactionType != null) {
                            val typingText =
                                if (!peerType.isChat() && interactiveUsers.size == 1) {
                                    when (interactionType) {
                                        InteractionType.File -> "Uploading file"
                                        InteractionType.Photo -> "Uploading photo"
                                        InteractionType.Typing -> "Typing"
                                        InteractionType.Video -> "Uploading Video"
                                        InteractionType.VoiceMessage -> "Recording voice message"
                                    }
                                } else {
                                    "$interactiveUsers are typing"
                                }

                            Text(
                                text = typingText,
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
                            attachmentImage?.let { drawable ->
                                Column {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Image(
                                        modifier = Modifier.size(14.dp),
                                        painter = rememberDrawablePainter(drawable),
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
                            text = date,
                            style = MaterialTheme.typography.bodySmall
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

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


