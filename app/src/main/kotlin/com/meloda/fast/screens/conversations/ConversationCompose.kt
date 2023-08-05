package com.meloda.fast.screens.conversations

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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.ext.combinedClickableSound
import com.meloda.fast.ext.getString
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.getImage
import com.meloda.fast.ui.widgets.CoilImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Conversation(
    onItemClick: (VkConversationUi) -> Unit,
    onItemLongClick: (VkConversationUi) -> Unit,
    conversation: VkConversationUi,
    maxLines: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickableSound(
                onClick = { onItemClick(conversation) },
                onLongClick = { onItemLongClick(conversation) }
            )
    ) {
        if (conversation.isUnread) {
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

                    if (conversation.id == UserConfig.userId) {
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
                        Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            painter = painterResource(id = R.drawable.ic_account_circle_cut),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                        )
                        CoilImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentDescription = null,
                            model = conversation.avatar.getImage(),
                            previewPainter = painterResource(id = R.drawable.ic_account_circle_cut),
                        )
                    }

                    if (conversation.isPinned) {
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
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = conversation.title.getString().orDots(),
                        modifier = Modifier,
                        minLines = 1,
                        maxLines = maxLines,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp)
                    )

                    Row {
                        conversation.attachmentImage?.getResourceId()?.let { resId ->
                            Column {
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    modifier = Modifier.size(14.dp),
                                    painter = painterResource(id = resId),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))
                        }

                        Text(
                            modifier = Modifier.weight(1f),
                            text = conversation.message,
                            minLines = 1,
                            maxLines = maxLines,
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(text = conversation.date)

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

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
