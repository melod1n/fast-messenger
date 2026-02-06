package dev.meloda.fast.convos.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.skydoves.compose.stability.runtime.TraceRecomposition
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.convos.model.ConvosScreenState
import dev.meloda.fast.convos.navigation.ConvoGraph
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FullScreenContainedLoader
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.model.vk.ConvoOption
import dev.meloda.fast.ui.model.vk.UiConvo
import dev.meloda.fast.ui.theme.LocalBottomPadding
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalReselectedTab
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList
import dev.meloda.fast.ui.util.isScrollingUp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3ExpressiveApi::class,
)
@Composable
fun ConvosScreen(
    screenState: ConvosScreenState = ConvosScreenState.EMPTY,
    convos: ImmutableList<UiConvo> = emptyImmutableList(),
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    onBack: () -> Unit = {},
    onConvoItemClicked: (convo: UiConvo) -> Unit = {},
    onConvoItemLongClicked: (convo: UiConvo) -> Unit = {},
    onOptionClicked: (UiConvo, ConvoOption) -> Unit = { _, _ -> },
    onPaginationConditionsMet: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onCreateChatButtonClicked: () -> Unit = {},
    onArchiveActionClicked: () -> Unit = {},
    setScrollIndex: (Int) -> Unit = {},
    setScrollOffset: (Int) -> Unit = {},
    onConsumeReselection: () -> Unit = {},
    onErrorViewButtonClicked: () -> Unit = {}
) {
    val currentTheme = LocalThemeConfig.current
    val maxLines = if (currentTheme.enableMultiline) 2 else 1

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = screenState.scrollIndex,
        initialFirstVisibleItemScrollOffset = screenState.scrollOffset
    )

    val currentTabReselected = LocalReselectedTab.current[ConvoGraph] == true
    LaunchedEffect(currentTabReselected) {
        if (currentTabReselected) {
            if (screenState.isArchive) {
                onBack.invoke()
            } else {
                if (listState.firstVisibleItemIndex > 14) {
                    listState.scrollToItem(14)
                }
                listState.animateScrollToItem(0)
                onConsumeReselection()
            }
        }
    }

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

    var dropDownMenuExpanded by remember {
        mutableStateOf(false)
    }

    val toolbarColorAlpha by animateFloatAsState(
        targetValue = if (!listState.canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    val toolbarContainerColor by animateColorAsState(
        targetValue =
            if (!listState.canScrollBackward) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                id = when {
                                    screenState.isLoading -> R.string.title_loading
                                    screenState.isArchive -> R.string.title_archive
                                    else -> R.string.title_convos
                                }
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        if (screenState.isArchive) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_back_round_24),
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    actions = {
                        if (!screenState.isArchive) {
                            IconButton(onClick = onArchiveActionClicked) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_archive_round_24),
                                    contentDescription = null
                                )
                            }
                        }

                        val dropDownItems = mutableListOf<@Composable () -> Unit>()

                        if (AppSettings.General.showManualRefreshOptions) {
                            dropDownItems += {
                                DropdownMenuItem(
                                    onClick = {
                                        onRefresh()
                                        dropDownMenuExpanded = false
                                    },
                                    text = {
                                        Text(text = stringResource(id = R.string.action_refresh))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_refresh_round_24),
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }

                        if (dropDownItems.isNotEmpty()) {
                            IconButton(onClick = { dropDownMenuExpanded = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_vert_round_24),
                                    contentDescription = null
                                )
                            }
                        }

                        DropdownMenu(
                            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                            expanded = dropDownMenuExpanded,
                            onDismissRequest = { dropDownMenuExpanded = false },
                            offset = DpOffset(x = (-4).dp, y = (-60).dp)
                        ) {
                            dropDownItems.forEach { it.invoke() }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = toolbarContainerColor.copy(
                            alpha = if (currentTheme.enableBlur) toolbarColorAlpha else 1f
                        )
                    ),
                    modifier = Modifier
                        .then(
                            if (currentTheme.enableBlur) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.regular(toolbarContainerColor)
                                )
                            } else Modifier
                        )
                        .fillMaxWidth(),
                )

                val showHorizontalProgressBar by remember(screenState) {
                    derivedStateOf { screenState.isLoading && convos.isNotEmpty() }
                }
                AnimatedVisibility(showHorizontalProgressBar) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                AnimatedVisibility(!showHorizontalProgressBar) {
                    HorizontalDivider()
                }
            }
        },
        floatingActionButton = {
            if (!screenState.isArchive) {
                val offsetY by animateIntAsState(
                    targetValue = if (listState.isScrollingUp()) 0 else 600
                )

                Column {
                    FloatingActionButton(
                        onClick = onCreateChatButtonClicked,
                        modifier = Modifier.offset {
                            IntOffset(0, offsetY)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_round_24),
                            contentDescription = "Add chat button"
                        )
                    }

                    Spacer(modifier = Modifier.height(LocalBottomPadding.current))
                }
            }
        }
    ) { padding ->
        when {
            baseError != null -> {
                VkErrorView(
                    baseError = baseError,
                    onButtonClick = onErrorViewButtonClicked
                )
            }

            screenState.isLoading && convos.isEmpty() -> FullScreenContainedLoader()

            else -> {
                val pullToRefreshState = rememberPullToRefreshState()

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
                    ConvosList(
                        convos = convos,
                        onConvosClick = onConvoItemClicked,
                        onConvosLongClick = onConvoItemLongClicked,
                        screenState = screenState,
                        state = listState,
                        maxLines = maxLines,
                        modifier = if (currentTheme.enableBlur) {
                            Modifier.hazeSource(state = hazeState)
                        } else {
                            Modifier
                        }.fillMaxSize(),
                        onOptionClicked = onOptionClicked,
                        padding = padding
                    )

                    if (convos.isEmpty()) {
                        NoItemsView(
                            buttonText = stringResource(R.string.action_refresh),
                            onButtonClick = onRefresh
                        )
                    }
                }
            }
        }
    }
}
