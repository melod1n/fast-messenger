package com.meloda.app.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.messageshistory.model.UiItem
import com.meloda.app.fast.ui.theme.LocalThemeConfig
import com.meloda.app.fast.ui.util.ImmutableList
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    listState: LazyListState,
    immutableMessages: ImmutableList<UiItem>,
    isPaginating: Boolean,
    enableAnimations: Boolean,
    messageBarHeight: Dp,
    onRequestScrollToCmId: (cmId: Int) -> Unit = {}
) {
    val messages = immutableMessages.toList()
    val currentTheme = LocalThemeConfig.current

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (currentTheme.enableBlur) {
                    Modifier.haze(
                        state = hazeState,
                        style = HazeMaterials.regular()
                    )
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
                        item = item,
                        onClick = {
                            if (item.actionCmId != null) {
                                onRequestScrollToCmId(item.actionCmId)
                            }
                        }
                    )
                }

                is UiItem.Message -> {
                    if (item.isOut) {
                        OutgoingMessageBubble(
                            modifier =
                            Modifier.then(
                                if (enableAnimations) Modifier.animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null
                                )
                                else Modifier
                            ),
                            message = item,
                        )
                    } else {
                        IncomingMessageBubble(
                            modifier =
                            Modifier.then(
                                if (enableAnimations) Modifier.animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null
                                )
                                else Modifier
                            ),
                            message = item,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )
            Spacer(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
            )
        }
    }
}
