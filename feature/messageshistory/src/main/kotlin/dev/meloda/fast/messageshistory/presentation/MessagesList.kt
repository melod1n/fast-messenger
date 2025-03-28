package dev.meloda.fast.messageshistory.presentation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    hasPinnedMessage: Boolean,
    hazeState: HazeState,
    listState: LazyListState,
    immutableMessages: ImmutableList<UiItem>,
    isPaginating: Boolean,
    messageBarHeight: Dp,
    onRequestScrollToCmId: (cmId: Int) -> Unit = {},
    onMessageClicked: (Int) -> Unit = {},
    onMessageLongClicked: (Int) -> Unit = {}
) {
    val messages = remember(immutableMessages) {
        immutableMessages.toList()
    }
    val theme = LocalThemeConfig.current
    val view = LocalView.current

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
            Spacer(modifier = Modifier.height(messageBarHeight.plus(18.dp)))
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            )
        }

        items(
            items = messages,
            key = UiItem::id,
            contentType = { item ->
                when (item) {
                    is UiItem.ActionMessage -> "action_message"
                    is UiItem.Message -> "message"
                }
            }
        ) { item ->
            when (item) {
                is UiItem.ActionMessage -> {
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
                                onRequestScrollToCmId(item.actionCmId)
                            }
                        }
                    )
                }

                is UiItem.Message -> {
                    val backgroundColor by animateColorAsState(
                        targetValue = if (item.isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        } else {
                            Color.Transparent
                        }
                    )

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
                            ),
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
                                message = item
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
                                message = item
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
