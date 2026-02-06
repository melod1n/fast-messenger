package dev.meloda.fast.messageshistory.presentation

import android.content.Intent
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.view.HapticFeedbackConstantsCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.model.api.domain.VkLinkDomain
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.ui.model.vk.MessageUiItem
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    hasPinnedMessage: Boolean,
    hazeState: HazeState,
    listState: LazyListState,
    uiMessages: ImmutableList<MessageUiItem>,
    isSelectedAtLeastOne: Boolean,
    isPaginating: Boolean,
    isReplying: Boolean,
    messageBarHeight: Dp,
    onRequestScrollToCmId: (cmId: Long) -> Unit = {},
    onMessageClicked: (Long) -> Unit = {},
    onMessageLongClicked: (Long) -> Unit = {},
    onPhotoClicked: (images: List<String>, index: Int) -> Unit = { _, _ -> },
    onRequestMessageReply: (cmId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val theme = LocalThemeConfig.current
    val view = LocalView.current

    val scope = rememberCoroutineScope()

    val onAttachmentClick by rememberUpdatedState { message: MessageUiItem.Message, attachment: VkAttachment ->
        if (isSelectedAtLeastOne) {
            onMessageClicked(message.id)
        } else {
            when (attachment) {
                is VkPhotoDomain -> {
                    val photos = message.attachments
                        .orEmpty()
                        .filterIsInstance<VkPhotoDomain>()
                        .mapNotNull { photo -> photo.getMaxSize()?.url }

                    onPhotoClicked(
                        photos,
                        photos.indexOfFirst { it == attachment.getMaxSize()?.url }
                    )

//                        val maxSize = attachment.getMaxSize()
//                        maxSize?.let {
//                            context.startActivity(
//                                Intent(Intent.ACTION_VIEW, maxSize.url.toUri())
//                            )
//                        }
                }

                is VkFileDomain -> {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, attachment.url.toUri())
                    )
                }

                is VkLinkDomain -> {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, attachment.url.toUri())
                    )
                }
            }
        }
    }

    val onAttachmentLongClick by rememberUpdatedState { message: MessageUiItem.Message, attachment: VkAttachment ->
        if (isSelectedAtLeastOne) {
            onMessageLongClicked(message.id)
            uiMessages
        } else {
            when (attachment) {
                is VkPhotoDomain -> {
                    val maxSize = attachment.getMaxSize()
                    Log.d("MessagesList", "onPhotoLongClicked. Max size: ${maxSize?.url}")
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (theme.enableBlur) {
                    Modifier.hazeSource(state = hazeState)
                } else Modifier
            ),
        state = listState,
        reverseLayout = true
    ) {
        item {
            AnimatedVisibility(isReplying) {
                Spacer(modifier = Modifier.height(48.dp))
            }

            Spacer(modifier = Modifier.height(messageBarHeight.plus(12.dp)))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            )
        }

        items(
            items = uiMessages.values,
            key = MessageUiItem::id,
            contentType = { item ->
                when (item) {
                    is MessageUiItem.ActionMessage -> "action_message"
                    is MessageUiItem.Message -> "message"
                }
            }
        ) { item ->
            when (item) {
                is MessageUiItem.ActionMessage -> {
                    ActionMessageItem(
                        modifier = Modifier.then(
                            if (theme.enableAnimations) Modifier.animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null
                            ) else Modifier
                        ),
                        item = item,
                        onClick = {
                            if (item.actionCmId != null) {
                                onRequestScrollToCmId(item.actionCmId!!)
                            }
                        }
                    )
                }

                is MessageUiItem.Message -> {
                    val backgroundColor by animateColorAsState(
                        targetValue = if (item.isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        } else {
                            Color.Transparent
                        }
                    )

                    var animate by remember { mutableStateOf(false) }
                    var offsetX by remember { mutableFloatStateOf(0f) }
                    var offsetDistinct by remember { mutableFloatStateOf(0f) }
                    val offsetAnimatable = remember { Animatable(0f) }

                    LaunchedEffect(offsetX) {
                        if (!animate) {
                            offsetAnimatable.snapTo(offsetX)
                        }
                    }

                    LaunchedEffect(Unit) {
                        snapshotFlow { offsetX.minus(5f).coerceIn(-100f, 0f) }
                            .distinctUntilChanged()
                            .collect { offsetDistinct = it }
                    }

                    LaunchedEffect(offsetDistinct) {
                        if (offsetDistinct == -100f && AppSettings.General.enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .then(
                                if (theme.enableAnimations) Modifier.animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null
                                ) else Modifier
                            )
                            .combinedClickable(
                                onLongClick = {
                                    if (AppSettings.General.enableHaptic) {
                                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                    }
                                    onMessageLongClicked(item.id)
                                },
                                onClick = { onMessageClicked(item.id) }
                            )
                            .pointerInput(item.cmId) {
                                detectHorizontalDragGestures(
                                    onDragCancel = {
                                        if (offsetX == -100f) {
                                            onRequestMessageReply(item.cmId)
                                        }

                                        scope.launch {
                                            animate = true
                                            offsetX = 0f
                                            offsetAnimatable.animateTo(0f)
                                            animate = false
                                        }
                                    },
                                    onDragEnd = {
                                        if (offsetX == -100f) {
                                            onRequestMessageReply(item.cmId)
                                        }

                                        scope.launch {
                                            animate = true
                                            offsetX = 0f
                                            offsetAnimatable.animateTo(0f)
                                            animate = false
                                        }
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX = (offsetX + dragAmount).coerceIn(-100f, 0f)
                                    }
                                )
                            },
                        color = backgroundColor
                    ) {
                        if (item.isOut) {
                            OutgoingMessageBubble(
                                modifier =
                                    Modifier
                                        .padding(vertical = 4.dp)
                                        .then(
                                            if (theme.enableAnimations) Modifier.animateItem(
                                                fadeInSpec = null,
                                                fadeOutSpec = null
                                            )
                                            else Modifier
                                        ),
                                enableAnimations = theme.enableAnimations,
                                message = item,
                                onClick = { attachment ->
                                    onAttachmentClick(item, attachment)
                                },
                                onLongClick = { attachment ->
                                    onAttachmentLongClick(item, attachment)
                                },
                                onReplyClick = {
                                    if (item.replyCmId != null) {
                                        onRequestScrollToCmId(item.replyCmId!!)
                                    }
                                },
                                offsetX = offsetAnimatable.value
                            )
                        } else {
                            IncomingMessageBubble(
                                modifier =
                                    Modifier
                                        .padding(vertical = 4.dp)
                                        .then(
                                            if (theme.enableAnimations) Modifier.animateItem(
                                                fadeInSpec = null,
                                                fadeOutSpec = null
                                            )
                                            else Modifier
                                        ),
                                enableAnimations = theme.enableAnimations,
                                message = item,
                                onClick = { attachment ->
                                    onAttachmentClick(item, attachment)
                                },
                                onLongClick = { attachment ->
                                    onAttachmentLongClick(item, attachment)
                                },
                                onReplyClick = {
                                    if (item.replyCmId != null) {
                                        onRequestScrollToCmId(item.replyCmId!!)
                                    }
                                },
                                offsetX = offsetAnimatable.value
                            )
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(isPaginating) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            if (hasPinnedMessage) {
                Spacer(modifier = Modifier.height(56.dp))
            }

            Spacer(Modifier.height(8.dp))
            Spacer(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )
        }
    }
}
