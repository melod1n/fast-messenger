package com.meloda.app.fast.friends.presentation

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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.friends.FriendsViewModel
import com.meloda.app.fast.friends.FriendsViewModelImpl
import com.meloda.app.fast.friends.model.FriendsScreenState
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.ui.components.ErrorView
import com.meloda.app.fast.ui.components.FullScreenLoader
import com.meloda.app.fast.ui.components.NoItemsView
import com.meloda.app.fast.ui.model.TabItem
import com.meloda.app.fast.ui.theme.LocalHazeState
import com.meloda.app.fast.ui.theme.LocalThemeConfig
import com.meloda.app.fast.ui.util.ImmutableList
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.meloda.app.fast.ui.R as UiR

@Composable
fun FriendsRoute(
    onError: (BaseError) -> Unit,
    viewModel: FriendsViewModel = koinViewModel<FriendsViewModelImpl>()
) {
    val context = LocalContext.current

    val userSettings: UserSettings = koinInject()

    val enablePullToRefresh by userSettings.enablePullToRefresh.collectAsStateWithLifecycle()

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
        enablePullToRefresh = enablePullToRefresh,
        canPaginate = canPaginate,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onRefresh = viewModel::onRefresh
    )
}


// TODO: 13/07/2024, Danil Nikolaev: support for online
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun FriendsScreen(
    screenState: FriendsScreenState = FriendsScreenState.EMPTY,
    baseError: BaseError? = null,
    enablePullToRefresh: Boolean = false,
    canPaginate: Boolean = false,
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val currentTheme = LocalThemeConfig.current

    val maxLines by remember {
        derivedStateOf {
            if (currentTheme.enableMultiline) 2 else 1
        }
    }

    val listState = rememberLazyListState()

    val paginationConditionMet by remember {
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

    val topBarContainerColorAlpha by animateFloatAsState(
        targetValue = if (!currentTheme.enableBlur || !listState.canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val topBarContainerColor by animateColorAsState(
        targetValue =
        if (currentTheme.enableBlur || !listState.canScrollBackward)
            MaterialTheme.colorScheme.surface
        else
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

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
                            Modifier.hazeChild(
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
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier,
                    containerColor = Color.Transparent
                ) {
                    tabItems.forEachIndexed { index, item ->
                        Tab(
                            selected = index == selectedTabIndex,
                            onClick = {
                                if (selectedTabIndex != index) {
                                    selectedTabIndex = index
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
            baseError is BaseError.SessionExpired -> {
                ErrorView(
                    text = "Session expired",
                    buttonText = "Log out",
                    onButtonClick = onSessionExpiredLogOutButtonClicked
                )
            }

            screenState.isLoading && screenState.friends.isEmpty() -> FullScreenLoader()

            else -> {
                val pagerState = rememberPagerState { tabItems.size }

                LaunchedEffect(selectedTabIndex) {
                    pagerState.animateScrollToPage(selectedTabIndex)
                }

                LaunchedEffect(pagerState) {
                    snapshotFlow {
                        pagerState.currentPage
                    }.collect { page ->
                        selectedTabIndex = page
                    }
                }

                val pullToRefreshState = rememberPullToRefreshState()

                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                                .padding(bottom = padding.calculateBottomPadding())
                                .then(
                                    if (enablePullToRefresh) {
                                        Modifier.nestedScroll(
                                            pullToRefreshState.nestedScrollConnection
                                        )
                                    } else Modifier
                                )
                        ) {
                            val friendsToDisplay = screenState.friends

                            FriendsList(
                                modifier = if (currentTheme.enableBlur) {
                                    Modifier.haze(
                                        state = hazeState,
                                        style = HazeMaterials.thick()
                                    )
                                } else {
                                    Modifier
                                }.fillMaxSize(),
                                screenState = screenState,
                                uiFriends = ImmutableList.copyOf(friendsToDisplay),
                                listState = listState,
                                maxLines = maxLines,
                                padding = padding
                            )

                            if (friendsToDisplay.isEmpty()) {
                                NoItemsView(
                                    modifier = Modifier
                                        .padding(padding.calculateTopPadding())
                                        .padding(top = 16.dp),
                                    customText = "No${if (index == 1) " online" else ""} friends :("
                                )
                            }

                            if (enablePullToRefresh) {
                                if (pullToRefreshState.isRefreshing) {
                                    LaunchedEffect(true) {
                                        onRefresh()
                                    }
                                }

                                LaunchedEffect(screenState.isLoading) {
                                    if (!screenState.isLoading) {
                                        pullToRefreshState.endRefresh()
                                    }
                                }

                                PullToRefreshContainer(
                                    state = pullToRefreshState,
                                    modifier = Modifier
                                        .padding(top = padding.calculateTopPadding())
                                        .align(Alignment.TopCenter),
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
