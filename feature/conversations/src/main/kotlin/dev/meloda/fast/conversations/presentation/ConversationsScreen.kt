package dev.meloda.fast.conversations.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.conversations.ConversationsViewModel
import dev.meloda.fast.conversations.ConversationsViewModelImpl
import dev.meloda.fast.conversations.model.ConversationOption
import dev.meloda.fast.conversations.model.ConversationsScreenState
import dev.meloda.fast.conversations.model.UiConversation
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.FullScreenLoader
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.theme.LocalBottomPadding
import dev.meloda.fast.ui.theme.LocalHazeState
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.isScrollingUp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import dev.meloda.fast.ui.R as UiR

@Composable
fun ConversationsRoute(
    onError: (BaseError) -> Unit,
    onConversationItemClicked: (conversationId: Int) -> Unit,
    onPhotoClicked: (url: String) -> Unit,
    viewModel: ConversationsViewModel = koinViewModel<ConversationsViewModelImpl>()
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

    ConversationsScreen(
        screenState = screenState,
        baseError = baseError,
        canPaginate = canPaginate,
        enablePullToRefresh = enablePullToRefresh,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onConversationItemClicked = { id ->
            onConversationItemClicked(id)
            viewModel.onConversationItemClick()
        },
        onConversationItemLongClicked = viewModel::onConversationItemLongClick,
        onOptionClicked = viewModel::onOptionClicked,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onRefreshDropdownItemClicked = viewModel::onRefresh,
        onRefresh = viewModel::onRefresh,
        onPhotoClicked = onPhotoClicked
    )


    HandleDialogs(
        screenState = screenState,
        viewModel = viewModel
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
fun ConversationsScreen(
    screenState: ConversationsScreenState = ConversationsScreenState.EMPTY,
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    enablePullToRefresh: Boolean = false,
    onSessionExpiredLogOutButtonClicked: () -> Unit,
    onConversationItemClicked: (conversationId: Int) -> Unit = {},
    onConversationItemLongClicked: (conversation: UiConversation) -> Unit = {},
    onOptionClicked: (UiConversation, ConversationOption) -> Unit = { _, _ -> },
    onPaginationConditionsMet: () -> Unit = {},
    onRefreshDropdownItemClicked: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onPhotoClicked: (url: String) -> Unit = {}
) {
    val view = LocalView.current
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
                    title = {
                        Text(
                            text = stringResource(
                                id = if (screenState.isLoading) UiR.string.title_loading
                                else UiR.string.title_conversations
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                dropDownMenuExpanded = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Options button"
                            )
                        }

                        DropdownMenu(
                            modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                            expanded = dropDownMenuExpanded,
                            onDismissRequest = {
                                dropDownMenuExpanded = false
                            },
                            offset = DpOffset(x = (-4).dp, y = (-60).dp)
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    onRefreshDropdownItemClicked()
                                    dropDownMenuExpanded = false
                                },
                                text = {
                                    Text(text = stringResource(id = UiR.string.action_refresh))
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null
                                    )
                                }
                            )
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
                                Modifier.hazeChild(
                                    state = hazeState,
                                    style = HazeMaterials.thick()
                                )
                            } else Modifier
                        )
                        .fillMaxWidth(),
                )

                val showHorizontalProgressBar by remember(screenState) {
                    derivedStateOf { screenState.isLoading && screenState.conversations.isNotEmpty() }
                }
                AnimatedVisibility(showHorizontalProgressBar) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            val scope = rememberCoroutineScope()
            val rotation = remember { Animatable(0f) }

            Column {
                AnimatedVisibility(
                    visible = listState.isScrollingUp(),
                    enter = slideIn { IntOffset(0, 600) } + fadeIn(tween(200)),
                    exit = slideOut { IntOffset(0, 600) } + fadeOut(tween(200))
                ) {
                    FloatingActionButton(
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)

                            scope.launch {
                                for (i in 20 downTo 0 step 4) {
                                    rotation.animateTo(
                                        targetValue = i.toFloat(),
                                        animationSpec = tween(50)
                                    )
                                    if (i > 0) {
                                        rotation.animateTo(
                                            targetValue = -i.toFloat(),
                                            animationSpec = tween(50)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.rotate(rotation.value)
                    ) {
                        Icon(
                            painter = painterResource(id = UiR.drawable.ic_baseline_create_24),
                            contentDescription = "Add chat button"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(LocalBottomPadding.current))
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

            screenState.isLoading && screenState.conversations.isEmpty() -> FullScreenLoader()

            else -> {
                val pullToRefreshState = rememberPullToRefreshState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                        .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                        .padding(bottom = padding.calculateBottomPadding())
                        .then(
                            if (enablePullToRefresh) {
                                Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection)
                            } else Modifier
                        )
                ) {
                    ConversationsList(
                        onConversationsClick = onConversationItemClicked,
                        onConversationsLongClick = onConversationItemLongClicked,
                        screenState = screenState,
                        state = listState,
                        maxLines = maxLines,
                        modifier = if (currentTheme.enableBlur) {
                            Modifier.haze(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else {
                            Modifier
                        }.fillMaxSize(),
                        onOptionClicked = onOptionClicked,
                        padding = padding,
                        onPhotoClicked = onPhotoClicked
                    )

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
                                .align(Alignment.TopCenter)
                                .padding(top = padding.calculateTopPadding()),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// TODO: 26.08.2023, Danil Nikolaev: remove usage of viewModel
@Composable
fun HandleDialogs(
    screenState: ConversationsScreenState,
    viewModel: ConversationsViewModel
) {
    val showOptions = screenState.showOptions

    if (showOptions.showDeleteDialog != null) {
        MaterialDialog(
            onDismissRequest = viewModel::onDeleteDialogDismissed,
            title = stringResource(id = UiR.string.confirm_delete_conversation),
            confirmAction = viewModel::onDeleteDialogPositiveClick,
            confirmText = stringResource(id = UiR.string.action_delete),
            cancelText = stringResource(id = UiR.string.cancel)
        )
    }

    if (showOptions.showPinDialog != null) {
        val conversation = showOptions.showPinDialog

        MaterialDialog(
            onDismissRequest = viewModel::onPinDialogDismissed,
            title = stringResource(
                id = if (conversation.isPinned) UiR.string.confirm_unpin_conversation
                else UiR.string.confirm_pin_conversation
            ),
            confirmAction = viewModel::onPinDialogPositiveClick,
            confirmText = stringResource(
                id = if (conversation.isPinned) UiR.string.action_unpin
                else UiR.string.action_pin
            ),
            cancelText = stringResource(id = UiR.string.cancel)
        )
    }
}
