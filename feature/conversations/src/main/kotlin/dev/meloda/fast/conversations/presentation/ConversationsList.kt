package dev.meloda.fast.conversations.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.meloda.fast.conversations.model.ConversationsScreenState
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.api.ConversationOption
import dev.meloda.fast.ui.model.api.UiConversation
import dev.meloda.fast.ui.theme.LocalBottomPadding
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ConversationsList(
    modifier: Modifier = Modifier,
    conversations: ImmutableList<UiConversation>,
    onConversationsClick: (UiConversation) -> Unit,
    onConversationsLongClick: (UiConversation) -> Unit,
    screenState: ConversationsScreenState,
    state: LazyListState,
    maxLines: Int,
    onOptionClicked: (UiConversation, ConversationOption) -> Unit,
    padding: PaddingValues
) {
    val theme = LocalThemeConfig.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(
            items = conversations.values,
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
                modifier =
                    if (theme.enableAnimations) Modifier.animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null
                    )
                    else Modifier
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (theme.enableAnimations)
                            Modifier.animateItem(
                                fadeInSpec = null,
                                fadeOutSpec = null
                            )
                        else Modifier
                    ),
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
                            painter = painterResource(R.drawable.round_keyboard_arrow_up_24px),
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(LocalBottomPadding.current))
            }
        }
    }
}
