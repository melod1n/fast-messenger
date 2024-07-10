package com.meloda.app.fast.friends.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.meloda.app.fast.designsystem.LocalTheme
import com.meloda.app.fast.designsystem.components.BlurrableTopAppBar
import com.meloda.app.fast.designsystem.components.FullScreenLoader
import com.meloda.app.fast.friends.FriendsViewModel
import com.meloda.app.fast.friends.FriendsViewModelImpl
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.ui.ErrorView
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import org.koin.androidx.compose.koinViewModel

import com.meloda.app.fast.designsystem.R as UiR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun FriendsScreen(
    onError: (BaseError) -> Unit,
    viewModel: FriendsViewModel = koinViewModel<FriendsViewModelImpl>()
) {
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val imagesToPreload by viewModel.imagesToPreload.collectAsStateWithLifecycle()
    imagesToPreload.forEach { url ->
        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(url)
                .build()
        )
    }

    val view = LocalView.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    val currentTheme = LocalTheme.current

    val maxLines by remember {
        derivedStateOf {
            if (currentTheme.multiline) 2 else 1
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
            viewModel.onMetPaginationCondition()
        }
    }

    val hazeState = remember { HazeState() }

    val pullToRefreshAlpha by animateFloatAsState(
        targetValue = if (!listState.canScrollBackward) 1f else 0f,
        label = "pullToRefreshAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                BlurrableTopAppBar(
                    title = stringResource(
                        id = if (screenState.isLoading) UiR.string.title_loading
                        else UiR.string.title_friends
                    ),
                    listState = listState,
                    hazeState = hazeState
                )

                val showHorizontalProgressBar by remember(screenState) {
                    derivedStateOf { screenState.isLoading && screenState.friends.isNotEmpty() }
                }
                AnimatedVisibility(showHorizontalProgressBar) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    ) { padding ->
        when {
            baseError is BaseError.SessionExpired -> {
                ErrorView(
                    text = "Session expired",
                    buttonText = "Log out",
                    onButtonClick = { onError(BaseError.SessionExpired) }
                )
            }

            screenState.isLoading && screenState.friends.isEmpty() -> FullScreenLoader()

            else -> {
                val pullToRefreshState = rememberPullToRefreshState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                        .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                        .padding(bottom = padding.calculateBottomPadding())
                        .nestedScroll(pullToRefreshState.nestedScrollConnection)
                ) {
                    FriendsList(
                        modifier = if (currentTheme.usingBlur) {
                            Modifier.haze(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else {
                            Modifier
                        }.fillMaxSize(),
                        screenState = screenState,
                        listState = listState,
                        maxLines = maxLines,
                        padding = padding
                    )

                    if (pullToRefreshState.isRefreshing) {
                        LaunchedEffect(true) {
                            viewModel.onRefresh()
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
                            .alpha(pullToRefreshAlpha)
                            .align(Alignment.TopCenter)
                            .padding(top = padding.calculateTopPadding()),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
