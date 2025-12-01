package dev.meloda.fast.friends.presentation

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.meloda.fast.friends.model.FriendsScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.api.UiFriend
import dev.meloda.fast.ui.theme.LocalBottomPadding
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FriendsList(
    modifier: Modifier = Modifier,
    screenState: FriendsScreenState,
    uiFriends: ImmutableList<UiFriend>,
    listState: LazyListState,
    maxLines: Int,
    padding: PaddingValues,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userid: Long) -> Unit,
    setCanScrollBackward: (Boolean) -> Unit
) {
    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollBackward }
            .collect(setCanScrollBackward)
    }

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            items = uiFriends.toList(),
            key = UiFriend::userId,
        ) { friend ->
            FriendItem(
                friend = friend,
                maxLines = maxLines,
                onPhotoClicked = onPhotoClicked,
                onMessageClicked = onMessageClicked
            )

            Spacer(modifier = Modifier.height(16.dp))
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
                                listState.scrollToItem(14)
                                listState.animateScrollToItem(0)
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
