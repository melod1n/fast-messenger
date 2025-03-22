package dev.meloda.fast.conversations.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.conversations.CreateChatViewModel
import dev.meloda.fast.conversations.model.CreateChatScreenState
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.FullScreenLoader
import dev.meloda.fast.ui.components.IconButton
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.theme.LocalBottomPadding
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.isScrollingUp
import dev.meloda.fast.ui.R as UiR

@Composable
fun CreateChatRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onChatCreated: (Int) -> Unit,
    viewModel: CreateChatViewModel
) {
    val context = LocalContext.current

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()
    val isChatCreated by viewModel.isChatCreated.collectAsStateWithLifecycle()

    LaunchedEffect(isChatCreated) {
        if (isChatCreated != null) {
            onChatCreated(isChatCreated ?: -1)
            viewModel.onNavigatedBack()
        }
    }

    CreateChatScreen(
        screenState = screenState,
        baseError = baseError,
        canPaginate = canPaginate,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onBack = onBack,
        onRefresh = viewModel::onRefresh,
        onCreateChatButtonClicked = viewModel::onCreateChatButtonClicked,
        onItemClicked = viewModel::toggleFriendSelection
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
fun CreateChatScreen(
    screenState: CreateChatScreenState = CreateChatScreenState.EMPTY,
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onCreateChatButtonClicked: () -> Unit = {},
    onItemClicked: (Int) -> Unit = {},
    onTitleTextInputChanged: (String) -> Unit = {}
) {
    val currentTheme = LocalThemeConfig.current

    val maxLines by remember(currentTheme) {
        mutableIntStateOf(if (currentTheme.enableMultiline) 2 else 1)
    }

    val listState = rememberLazyListState()

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

    val toolbarColorAlpha by animateFloatAsState(
        targetValue = if (!listState.canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    val toolbarContainerColor by animateColorAsState(
        targetValue =
            if (currentTheme.enableBlur || !listState.canScrollBackward)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(
                                id = if (screenState.isLoading) UiR.string.title_loading
                                else UiR.string.title_create_chat
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall
                        )
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
                                    style = HazeMaterials.thick()
                                )
                            } else Modifier
                        )
                        .fillMaxWidth(),
                )
            }
        },
        floatingActionButton = {
            if (baseError == null) {
                Column {
                    ExtendedFloatingActionButton(
                        onClick = onCreateChatButtonClicked,
                        expanded = listState.isScrollingUp(),
                        text = { Text(text = stringResource(UiR.string.action_create)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Done,
                                contentDescription = null
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(LocalBottomPadding.current))
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                        .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                        .padding(bottom = padding.calculateBottomPadding())
                ) {
                    TextField(
                        modifier = Modifier
                            .height(58.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        value = screenState.chatTitle,
                        onValueChange = onTitleTextInputChanged,
                        label = { Text(text = "Chat's title") },
                        placeholder = { Text(text = "Chat's title") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = UiR.drawable.outline_people_alt_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true
                    )

                    val pullToRefreshState = rememberPullToRefreshState()

                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
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
                        CreateChatList(
                            screenState = screenState,
                            state = listState,
                            maxLines = maxLines,
                            modifier = if (currentTheme.enableBlur) {
                                Modifier.hazeSource(state = hazeState)
                            } else {
                                Modifier
                            }.fillMaxSize(),
                            padding = padding,
                            onItemClicked = onItemClicked
                        )

                        if (screenState.friends.isEmpty()) {
                            NoItemsView(
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
