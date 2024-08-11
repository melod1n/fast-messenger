package dev.meloda.fast.conversations.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.meloda.fast.conversations.model.ConversationOption
import dev.meloda.fast.conversations.model.ConversationsScreenState
import dev.meloda.fast.conversations.model.UiConversation
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.ui.theme.LocalBottomPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ConversationsList(
    onConversationsClick: (Int) -> Unit,
    onConversationsLongClick: (UiConversation) -> Unit,
    screenState: ConversationsScreenState,
    state: LazyListState,
    maxLines: Int,
    modifier: Modifier,
    onOptionClicked: (UiConversation, ConversationOption) -> Unit,
    padding: PaddingValues,
    onPhotoClicked: (url: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val conversations = screenState.conversations

    val bottomPadding = LocalBottomPadding.current

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
        }
        items(
            items = conversations,
            key = UiConversation::id,
        ) { conversation ->
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
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                onPhotoClicked = onPhotoClicked
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(fadeInSpec = null, fadeOutSpec = null)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (screenState.isPaginating) {
                    CircularProgressIndicator()
                }

                if (screenState.isPaginationExhausted) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.Main) {
                                state.scrollToItem(14)
                                state.animateScrollToItem(0)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(bottomPadding))
        }
    }
}
