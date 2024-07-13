package com.meloda.app.fast.friends.presentation

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.designsystem.ImmutableList
import com.meloda.app.fast.designsystem.LocalBottomPadding
import com.meloda.app.fast.friends.model.FriendsScreenState
import com.meloda.app.fast.friends.model.UiFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun FriendsList(
    modifier: Modifier = Modifier,
    screenState: FriendsScreenState,
    uiFriends: ImmutableList<UiFriend>,
    listState: LazyListState,
    maxLines: Int,
    padding: PaddingValues
) {
    val coroutineScope = rememberCoroutineScope()

    val friends = uiFriends.toList()

    val bottomPadding = LocalBottomPadding.current

    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        item {
            Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(
            items = friends,
            key = UiFriend::userId,
        ) { friend ->

            FriendItem(
                friend = friend,
                maxLines = maxLines
            )

            Spacer(modifier = Modifier.height(16.dp))
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
                                listState.scrollToItem(14)
                                listState.animateScrollToItem(0)
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
