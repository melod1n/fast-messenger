package dev.meloda.fast.chatmaterials.presentation.materials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.meloda.fast.chatmaterials.model.ChatMaterialsScreenState
import dev.meloda.fast.chatmaterials.model.UiChatMaterial
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.FullScreenContainedLoader
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMaterialsScreen(
    modifier: Modifier = Modifier,
    canPaginate: Boolean,
    screenState: ChatMaterialsScreenState,
    baseError: BaseError?,
    padding: PaddingValues,
    onRefresh: () -> Unit,
    onSessionExpiredLogOutButtonClicked: () -> Unit,
    setCanScrollBackward: (Boolean) -> Unit,
    onPaginationConditionsMet: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val hazeState = LocalHazeState.current
    val currentTheme = LocalThemeConfig.current
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollBackward }
            .collect(setCanScrollBackward)
    }

    val paginationConditionMet by remember(canPaginate, listState) {
        derivedStateOf {
            canPaginate &&
                    (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -9) >= (listState.layoutInfo.totalItemsCount - 6)
        }
    }

    LaunchedEffect(paginationConditionMet) {
        if (paginationConditionMet && !screenState.isPaginating) {
            onPaginationConditionsMet()
        }
    }

    when {
        baseError != null -> {
            VkErrorView(baseError = baseError)
        }

        screenState.isLoading && screenState.materials.isEmpty() -> FullScreenContainedLoader()

        else -> {
            PullToRefreshBox(
                modifier = modifier
                    .fillMaxSize()
                    .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                    .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr)),
                state = pullToRefreshState,
                isRefreshing = screenState.isLoading,
                onRefresh = onRefresh,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = screenState.isLoading,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = padding.calculateTopPadding()),
                    )
                }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .then(
                            if (currentTheme.enableBlur) {
                                Modifier.hazeSource(state = hazeState)
                            } else {
                                Modifier
                            }
                        )
                        .fillMaxSize()

                ) {
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
                    }
                    items(screenState.materials) { item ->
                        item as UiChatMaterial.Audio

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 64.dp)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .size(42.dp)
                                    .padding(4.dp),
                                painter = painterResource(R.drawable.round_play_arrow_24),
                                contentDescription = null,
                                tint = contentColorFor(MaterialTheme.colorScheme.primary)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                LocalContentAlpha(alpha = ContentAlpha.medium) {
                                    Text(
                                        text = item.artist,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Text(text = item.duration)
                        }
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
                                        imageVector = Icons.Rounded.KeyboardArrowUp,
                                        contentDescription = null
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
                    }
                }

                if (screenState.materials.isEmpty()) {
                    NoItemsView(
                        buttonText = stringResource(R.string.action_refresh),
                        onButtonClick = onRefresh
                    )
                }
            }
        }
    }
}
