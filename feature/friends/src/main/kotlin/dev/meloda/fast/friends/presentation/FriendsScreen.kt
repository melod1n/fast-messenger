package dev.meloda.fast.friends.presentation

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import dev.chrisbanes.haze.hazeSource
import dev.meloda.fast.friends.FriendsViewModel
import dev.meloda.fast.friends.navigation.Friends
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FullScreenContainedLoader
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalReselectedTab
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    modifier: Modifier = Modifier,
    orderType: String,
    padding: PaddingValues,
    tabIndex: Int,
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {},
    onMessageClicked: (userid: Long) -> Unit = {},
    setCanScrollBackward: (Boolean) -> Unit = {},
    onScrolledToTop: () -> Unit = {}
) {
    val context: Context = LocalContext.current

    LaunchedEffect(orderType) {
        viewModel.onOrderTypeChanged(orderType)
    }

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    val imagesToPreload by viewModel.imagesToPreload.collectAsStateWithLifecycle()
    LaunchedEffect(imagesToPreload) {
        imagesToPreload.forEach { url ->
            context.imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .build()
            )
        }
    }

    val currentTheme = LocalThemeConfig.current

    val maxLines by remember {
        derivedStateOf {
            if (currentTheme.enableMultiline) 2 else 1
        }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = screenState.scrollIndex,
        initialFirstVisibleItemScrollOffset = screenState.scrollOffset
    )

    val scrollToTop = LocalReselectedTab.current[Friends] ?: false
    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            if (listState.firstVisibleItemIndex > 14) {
                listState.scrollToItem(14)
            }
            listState.animateScrollToItem(0)
            onScrolledToTop()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .debounce(250L)
            .collectLatest(viewModel::setScrollIndex)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .debounce(250L)
            .collectLatest(viewModel::setScrollOffset)
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
            viewModel.onPaginationConditionsMet()
        }
    }

    val hazeState = LocalHazeState.current

    baseError?.let { error ->
        VkErrorView(baseError = error)
        return
    }

    when {
        screenState.isLoading && screenState.friends.isEmpty() -> FullScreenContainedLoader()

        else -> {
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                modifier = modifier
                    .fillMaxSize()
                    .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                    .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                    .padding(bottom = padding.calculateBottomPadding()),
                state = pullToRefreshState,
                isRefreshing = screenState.isLoading,
                onRefresh = viewModel::onRefresh,
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
                FriendsList(
                    modifier = if (currentTheme.enableBlur) {
                        Modifier.hazeSource(state = hazeState)
                    } else {
                        Modifier
                    }.fillMaxSize(),
                    screenState = screenState,
                    uiFriends = ImmutableList.copyOf(screenState.friends),
                    listState = listState,
                    maxLines = maxLines,
                    padding = padding,
                    onPhotoClicked = onPhotoClicked,
                    onMessageClicked = onMessageClicked,
                    setCanScrollBackward = setCanScrollBackward
                )

                if (screenState.friends.isEmpty()) {
                    NoItemsView(
                        customText = if (tabIndex == 1) stringResource(R.string.no_online_friends) else null,
                        buttonText = stringResource(R.string.action_refresh),
                        onButtonClick = viewModel::onRefresh
                    )
                }
            }
        }
    }
}
