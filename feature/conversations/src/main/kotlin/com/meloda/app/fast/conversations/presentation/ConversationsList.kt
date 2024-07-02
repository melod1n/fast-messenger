package com.meloda.app.fast.conversations.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.conversations.model.ConversationOption
import com.meloda.app.fast.conversations.model.ConversationsScreenState
import com.meloda.app.fast.conversations.model.UiConversation

@Composable
fun ConversationsListComposable(
    onConversationsClick: (Int) -> Unit,
    onConversationsLongClick: (UiConversation) -> Unit,
    screenState: ConversationsScreenState,
    state: LazyListState,
    maxLines: Int,
    modifier: Modifier,
    onOptionClicked: (UiConversation, ConversationOption) -> Unit,
    padding: PaddingValues
) {
    val conversations = screenState.conversations

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        itemsIndexed(
            items = conversations,
            key = { _, item -> item.id },
        ) { index, conversation ->

            val needToShowSpacer by remember(conversations) {
                derivedStateOf {
                    index == 0
                }
            }

            if (needToShowSpacer) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            }

            val isUserAccount by remember(conversation) {
                derivedStateOf {
                    conversation.id == UserConfig.userId
                }
            }

            ConversationItem(
                onItemClick = onConversationsClick,
                onItemLongClick = onConversationsLongClick,
                onOptionClicked = onOptionClicked,
                maxLines = maxLines,
                isUserAccount = isUserAccount,
                conversation = conversation,
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
            )

            val showDefaultSpacer by remember(conversations) {
                derivedStateOf { index < conversations.size - 1 }
            }

            if (showDefaultSpacer) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            val showBottomNavigationBarsSpacer by remember(conversations) {
                derivedStateOf { !screenState.isPaginating && index == conversations.size - 1 }
            }

            if (showBottomNavigationBarsSpacer) {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        item {
            AnimatedVisibility(screenState.isPaginating) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}
