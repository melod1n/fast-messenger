package dev.meloda.fast.conversations.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FullScreenContainedLoader
import dev.meloda.fast.ui.components.FastIconButton
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.components.NoItemsView
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.isScrollingUp

@Composable
fun CreateChatRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onChatCreated: (Long) -> Unit,
    viewModel: CreateChatViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()
    val isChatCreated by viewModel.isChatCreated.collectAsStateWithLifecycle()

    LaunchedEffect(isChatCreated) {
        if (isChatCreated != null) {
            onChatCreated(isChatCreated ?: -1L)
            viewModel.onNavigatedBack()
        }
    }

    if (screenState.showConfirmDialog) {
        MaterialDialog(
            onDismissRequest = viewModel::onConfirmDialogDismissed,
            title = stringResource(R.string.confirm),
            text = when {
                screenState.selectedFriendsIds.isEmpty() -> stringResource(
                    R.string.confirm_chat_create_empty_with_title,
                    viewModel.finalChatTitle.value
                )

                else -> stringResource(
                    R.string.confirm_chat_create_with_title,
                    viewModel.finalChatTitle.value
                )
            },
            confirmAction = viewModel::onConfirmDialogConfirmed,
            confirmText = stringResource(R.string.action_create),
            cancelText = stringResource(R.string.cancel)
        )
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
        onItemClicked = viewModel::toggleFriendSelection,
        onTitleTextInputChanged = viewModel::onTitleTextInputChanged
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
    onItemClicked: (Long) -> Unit = {},
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
            if (currentTheme.enableBlur || !listState.canScrollBackward) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
                    .then(
                        if (currentTheme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else Modifier
                    )
            ) {
                TopAppBar(
                    navigationIcon = {
                        FastIconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.round_arrow_back_24px),
                                contentDescription = null
                            )
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(
                                id = if (screenState.isLoading) R.string.title_loading
                                else R.string.title_create_chat
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth(),
                )

                var isTextFieldFocused by remember {
                    mutableStateOf(false)
                }

                val borderWidth by animateDpAsState(if (isTextFieldFocused) 1.5.dp else 0.dp)
                val borderColor by animateColorAsState(
                    if (isTextFieldFocused) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                )

                TextField(
                    modifier = Modifier
                        .height(58.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .border(
                            borderWidth,
                            borderColor,
                            RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .onFocusChanged { isTextFieldFocused = it.hasFocus },
                    value = screenState.chatTitle,
                    onValueChange = onTitleTextInputChanged,
                    label = { Text(text = stringResource(R.string.create_chat_title)) },
                    placeholder = { Text(text = stringResource(R.string.create_chat_title)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    )
                )
                Spacer(Modifier.height(16.dp))
            }
        },
        floatingActionButton = {
            if (baseError == null) {
                Column(
                    modifier = Modifier
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onCreateChatButtonClicked,
                        expanded = listState.isScrollingUp(),
                        text = { Text(text = stringResource(R.string.action_create)) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.round_check_24px),
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        when {
            baseError != null -> {
                VkErrorView(baseError = baseError)
            }

            screenState.isLoading && screenState.friends.isEmpty() -> FullScreenContainedLoader()

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
                        onItemClicked = onItemClicked,
                        onTitleTextInputChanged = onTitleTextInputChanged
                    )

                    if (screenState.friends.isEmpty()) {
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
