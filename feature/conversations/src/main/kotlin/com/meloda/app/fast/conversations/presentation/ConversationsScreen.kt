package com.meloda.app.fast.conversations.presentation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.conversations.ConversationsViewModel
import com.meloda.app.fast.conversations.ConversationsViewModelImpl
import com.meloda.app.fast.conversations.model.ConversationsScreenState
import com.meloda.app.fast.conversations.model.UiConversation
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.ui.ErrorView
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicatorDefaults
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import me.gingerninja.lazylist.hijacker.rememberLazyListStateHijacker
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.meloda.app.fast.designsystem.R as UiR

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
fun ConversationsScreen(
    onError: (BaseError) -> Unit,
    onNavigateToMessagesHistory: (conversationId: Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ConversationsViewModel = koinViewModel<ConversationsViewModelImpl>()
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
    val userSettings: UserSettings = koinInject()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val loadCount by viewModel.loadCount.collectAsStateWithLifecycle()
    val currentOffset by viewModel.currentOffset.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    val currentTheme by userSettings.theme.collectAsStateWithLifecycle()

    val multilineEnabled by userSettings.multiline.collectAsStateWithLifecycle()
    val maxLines by remember(multilineEnabled) {
        derivedStateOf {
            if (multilineEnabled) 2 else 1
        }
    }

    val listState = rememberLazyListState()

    // TODO: 26/11/2023, Danil Nikolaev: remove when fixed
    rememberLazyListStateHijacker(listState = listState)

    val paginationConditionMet by remember {
        derivedStateOf {
            canPaginate &&
                    (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: -9) >= (listState.layoutInfo.totalItemsCount - 6)
        }
    }
    Log.d(
        "ConversationsScreen",
        "paginationCondMet: $paginationConditionMet; size: ${screenState.conversations.size}"
    )

    LaunchedEffect(paginationConditionMet) {
        if (paginationConditionMet && !screenState.isPaginating) {
            viewModel.onMetPaginationCondition()
        }
    }

    var showPullRefresh by remember {
        mutableStateOf(true)
    }
    var showCountOffsetAlert by remember {
        mutableStateOf(false)
    }

    if (showCountOffsetAlert) {
        var count by remember {
            mutableStateOf(loadCount.toString())
        }
        var offset by remember {
            mutableStateOf(currentOffset.toString())
        }

        MaterialDialog(
            onDismissAction = { showCountOffsetAlert = false },
            confirmText = UiText.Simple("Apply"),
            confirmAction = {
                viewModel.onChangeCountAndOffset(
                    count.toIntOrNull() ?: 30,
                    offset.toIntOrNull() ?: 0
                )
            },
            cancelText = UiText.Resource(UiR.string.cancel)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = count,
                    onValueChange = { newText -> count = newText },
                    label = { Text(text = "Count") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = offset,
                    onValueChange = { newText -> offset = newText },
                    label = { Text(text = "Offset") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }
    }

    val hazeState = remember { HazeState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            var dropDownMenuExpanded by remember {
                mutableStateOf(false)
            }

            val actions: @Composable RowScope.() -> Unit = @Composable {
                IconButton(
                    onClick = {
                        dropDownMenuExpanded = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Options"
                    )
                }

                DropdownMenu(
                    modifier = Modifier.defaultMinSize(minWidth = 140.dp),
                    expanded = dropDownMenuExpanded,
                    onDismissRequest = {
                        dropDownMenuExpanded = false
                    },
                    offset = DpOffset(x = (10).dp, y = (-60).dp)
                ) {
                    DropdownMenuItem(
                        onClick = {
                            onNavigateToSettings()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = UiR.string.title_settings))
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            viewModel.onRefresh()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = UiR.string.action_refresh))
                        }
                    )

                    val isDebugMenuShown by userSettings.debugSettingsEnabled.collectAsStateWithLifecycle()

                    if (isDebugMenuShown) {
                        DropdownMenuItem(
                            text = {
                                Text(text = "Toggle pull to refresh")
                            },
                            onClick = {
                                showPullRefresh = !showPullRefresh
                                dropDownMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(text = "Change count and offset")
                            },
                            onClick = {
                                showCountOffsetAlert = true
                                dropDownMenuExpanded = false
                            }
                        )
                    }
                }
            }

            val toolbarColorAlpha by animateFloatAsState(
                targetValue = if (!listState.canScrollBackward) 1f else 0f,
                label = "toolbarColorAlpha",
                animationSpec = tween(durationMillis = 50)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (screenState.isLoading) "Loading..." else "Conversations",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(
                            alpha = if (currentTheme.usingBlur) toolbarColorAlpha else 1f
                        )
                    ),
                    modifier = Modifier
                        .then(
                            if (currentTheme.usingBlur) {
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
            AnimatedVisibility(
                visible = listState.isScrollingDown(),
                modifier = Modifier.navigationBarsPadding(),
                enter = slideIn { IntOffset(0, 400) },
                exit = slideOut { IntOffset(0, 400) }
            ) {
                FloatingActionButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_baseline_create_24),
                        contentDescription = "Add chat button"
                    )
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

            screenState.isLoading && screenState.conversations.isEmpty() -> Loader()

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                        .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                        .padding(bottom = padding.calculateBottomPadding())
                ) {
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = screenState.isLoading,
                        onRefresh = viewModel::onRefresh
                    )
                    val listModifier = remember(showPullRefresh) {
                        Modifier
                            .fillMaxSize()
                            .then(
                                if (showPullRefresh) Modifier.pullRefresh(pullRefreshState)
                                else Modifier
                            )
                    }

                    ConversationsListComposable(
                        onConversationsClick = { id ->
                            onNavigateToMessagesHistory(id)
                            viewModel.onConversationItemClick(id)
                        },
                        onConversationsLongClick = viewModel::onConversationItemLongClick,
                        screenState = screenState,
                        state = listState,
                        maxLines = maxLines,
                        modifier = listModifier.then(
                            if (currentTheme.usingBlur) {
                                Modifier.haze(
                                    state = hazeState,
                                    style = HazeMaterials.thick()
                                )
                            } else Modifier
                        ),
                        onOptionClicked = viewModel::onOptionClicked,
                        padding = padding
                    )

                    AnimatedVisibility(
                        visible = showPullRefresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = padding.calculateTopPadding())
                    ) {
                        PullRefreshIndicator(
                            refreshing = screenState.isLoading,
                            state = pullRefreshState,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .wrapContentSize(),
                            colors = PullRefreshIndicatorDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    2.dp
                                ),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        HandleDialogs(
            screenState = screenState,
            viewModel = viewModel
        )
    }
}

@Composable
fun Loader() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
        val conversationId = showOptions.showDeleteDialog
        DeleteDialog(
            conversationId = conversationId,
            viewModel = viewModel
        )
    }

    showOptions.showPinDialog?.let { conversation ->
        PinDialog(
            conversation = conversation,
            viewModel = viewModel
        )
    }
}

@Composable
fun DeleteDialog(
    conversationId: Int,
    viewModel: ConversationsViewModel
) {
    MaterialDialog(
        title = UiText.Resource(UiR.string.confirm_delete_conversation),
        confirmText = UiText.Resource(UiR.string.action_delete),
        confirmAction = { viewModel.onDeleteDialogPositiveClick(conversationId) },
        cancelText = UiText.Resource(UiR.string.cancel),
        onDismissAction = viewModel::onDeleteDialogDismissed
    )
}

@Composable
fun PinDialog(
    conversation: UiConversation,
    viewModel: ConversationsViewModel
) {
    MaterialDialog(
        title = UiText.Resource(
            if (conversation.isPinned) UiR.string.confirm_unpin_conversation
            else UiR.string.confirm_pin_conversation
        ),
        confirmText = UiText.Resource(
            if (conversation.isPinned) UiR.string.action_unpin
            else UiR.string.action_pin
        ),
        confirmAction = {
            viewModel.onPinDialogPositiveClick(conversation)
        },
        cancelText = UiText.Resource(UiR.string.cancel),
        onDismissAction = viewModel::onPinDialogDismissed
    )
}


@Composable
private fun LazyListState.isScrollingDown(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}
