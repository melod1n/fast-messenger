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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.meloda.fast.conversations.model.CreateChatScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.api.UiFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CreateChatList(
    screenState: CreateChatScreenState,
    state: LazyListState,
    maxLines: Int,
    modifier: Modifier,
    padding: PaddingValues,
    onItemClicked: (Int) -> Unit,
    onTitleTextInputChanged: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))

        }
        items(
            items = screenState.friends,
            key = UiFriend::userId,
        ) { friend ->
            CreateChatItem(
                maxLines = maxLines,
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                friend = friend,
                isSelected = screenState.selectedFriendsIds.contains(friend.userId),
                onItemClicked = onItemClicked
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(fadeInSpec = null, fadeOutSpec = null),
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

                Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
