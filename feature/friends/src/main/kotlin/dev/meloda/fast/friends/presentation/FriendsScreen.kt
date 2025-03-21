package dev.meloda.fast.friends.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.friends.FriendsViewModel
import dev.meloda.fast.friends.FriendsViewModelImpl
import dev.meloda.fast.friends.model.FriendsScreenState
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.FullScreenLoader
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.model.TabItem
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import org.koin.androidx.compose.koinViewModel
import dev.meloda.fast.ui.R as UiR

@Composable
fun FriendsRoute(
    onError: (BaseError) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    onMessageClicked: (userId: Int) -> Unit,
    viewModel: FriendsViewModel = koinViewModel<FriendsViewModelImpl>()
) {
    val context = LocalContext.current

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

    FriendsScreen(
        screenState = screenState,
        baseError = baseError,
        canPaginate = canPaginate,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onRefresh = viewModel::onRefresh,
        onPhotoClicked = onPhotoClicked,
        onMessageClicked = onMessageClicked,
        setSelectedTabIndex = viewModel::onTabSelected,
        setScrollIndex = viewModel::setScrollIndex,
        setScrollOffset = viewModel::setScrollOffset,
        setScrollIndexOnline = viewModel::setScrollIndexOnline,
        setScrollOffsetOnline = viewModel::setScrollOffsetOnline
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun FriendsScreen(
    screenState: FriendsScreenState = FriendsScreenState.EMPTY,
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {},
    onMessageClicked: (userId: Int) -> Unit = {},
    setSelectedTabIndex: (Int) -> Unit = {},
    setScrollIndex: (Int) -> Unit = {},
    setScrollOffset: (Int) -> Unit = {},
    setScrollIndexOnline: (Int) -> Unit = {},
    setScrollOffsetOnline: (Int) -> Unit = {}
) {
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
    val listStateOnline = rememberLazyListState(
        initialFirstVisibleItemIndex = screenState.scrollIndexOnline,
        initialFirstVisibleItemScrollOffset = screenState.scrollOffsetOnline
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .debounce(500L)
            .collectLatest(setScrollIndex)
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .debounce(500L)
            .collectLatest(setScrollOffset)
    }

    LaunchedEffect(listStateOnline) {
        snapshotFlow { listStateOnline.firstVisibleItemIndex }
            .debounce(500L)
            .collectLatest(setScrollIndexOnline)
    }

    LaunchedEffect(listStateOnline) {
        snapshotFlow { listStateOnline.firstVisibleItemScrollOffset }
            .debounce(500L)
            .collectLatest(setScrollOffsetOnline)
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

    val hazeState = LocalHazeState.current

    var canScrollBackward by remember {
        mutableStateOf(false)
    }

    val topBarContainerColorAlpha by animateFloatAsState(
        targetValue = if (!currentTheme.enableBlur || !canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val topBarContainerColor by animateColorAsState(
        targetValue = if (currentTheme.enableBlur || !canScrollBackward)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val tabItems = remember {
        listOf(
            TabItem(
                titleResId = UiR.string.title_friends_all,
                unselectedIconResId = null,
                selectedIconResId = null
            ),
            TabItem(
                titleResId = UiR.string.title_friends_online,
                unselectedIconResId = null,
                selectedIconResId = null
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier
                    .then(
                        if (currentTheme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else {
                            Modifier
                        }
                    )
                    .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
                    .fillMaxWidth()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = UiR.string.title_friends),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                PrimaryTabRow(
                    selectedTabIndex = screenState.selectedTabIndex,
                    modifier = Modifier,
                    containerColor = Color.Transparent
                ) {
                    tabItems.forEachIndexed { index, item ->
                        Tab(
                            selected = index == screenState.selectedTabIndex,
                            onClick = {
                                if (screenState.selectedTabIndex != index) {
                                    setSelectedTabIndex(index)
                                }
                            },
                            text = {
                                item.titleResId?.let { resId ->
                                    Text(text = stringResource(id = resId))
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            baseError != null -> {
                when (baseError) {
                    is BaseError.SessionExpired -> {
                        ErrorView(
                            text = stringResource(UiR.string.session_expired),
                            buttonText = stringResource(UiR.string.action_log_out),
                            onButtonClick = onSessionExpiredLogOutButtonClicked
                        )
                    }

                    is BaseError.SimpleError -> {
                        ErrorView(
                            text = baseError.message,
                            buttonText = stringResource(UiR.string.try_again),
                            onButtonClick = onRefresh
                        )
                    }
                }
            }

            screenState.isLoading && screenState.friends.isEmpty() -> FullScreenLoader()

            else -> {
                val pagerState = rememberPagerState(
                    initialPage = screenState.selectedTabIndex
                ) {
                    tabItems.size
                }

                LaunchedEffect(screenState.selectedTabIndex) {
                    pagerState.animateScrollToPage(screenState.selectedTabIndex)
                }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }
                        .collect(setSelectedTabIndex)
                }

                val pullToRefreshState = rememberPullToRefreshState()

                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { index ->
                        PullToRefreshBox(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                                .padding(bottom = padding.calculateBottomPadding()),
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
                            val friendsToDisplay = remember(index) {
                                if (index == 0) {
                                    screenState.friends
                                } else {
                                    screenState.onlineFriends
                                }
                            }

                            FriendsList(
                                modifier = if (currentTheme.enableBlur) {
                                    Modifier.hazeSource(state = hazeState)
                                } else {
                                    Modifier
                                }.fillMaxSize(),
                                screenState = screenState,
                                uiFriends = ImmutableList.copyOf(friendsToDisplay),
                                listState = if (index == 0) listState else listStateOnline,
                                maxLines = maxLines,
                                padding = padding,
                                onPhotoClicked = onPhotoClicked,
                                onMessageClicked = onMessageClicked,
                                setCanScrollBackward = { can ->
                                    canScrollBackward = can
                                }
                            )

                            if (friendsToDisplay.isEmpty()) {
                                NoItemsView(
                                    customText = if (index == 1) stringResource(UiR.string.no_online_friends) else null,
                                    buttonText = stringResource(UiR.string.action_refresh),
                                    onButtonClick = onRefresh
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
