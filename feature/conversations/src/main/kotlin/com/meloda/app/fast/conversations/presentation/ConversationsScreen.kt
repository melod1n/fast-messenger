package com.meloda.app.fast.conversations.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.extensions.orDots
import com.meloda.app.fast.conversations.ConversationsViewModel
import com.meloda.app.fast.conversations.ConversationsViewModelImpl
import com.meloda.app.fast.conversations.model.ConversationOption
import com.meloda.app.fast.conversations.model.ConversationsScreenState
import com.meloda.app.fast.conversations.model.NavigationAction
import com.meloda.app.fast.conversations.model.UiConversation
import com.meloda.app.fast.datastore.UserConfig
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.model.BaseError
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

internal typealias OnAction = (NavigationAction) -> Unit

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
)
@Composable
fun ConversationsScreen(
    onError: (BaseError) -> Unit,
    onAction: OnAction,
    viewModel: ConversationsViewModel = koinViewModel<ConversationsViewModelImpl>()
) {
    val view = LocalView.current
    val userSettings: UserSettings = koinInject()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val conversations = screenState.conversations

    val isLoading = screenState.isLoading

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

    var useLightList by remember {
        mutableStateOf(false)
    }
    var showOnlyPlaceholders by remember {
        mutableStateOf(false)
    }
    var showPullRefresh by remember {
        mutableStateOf(true)
    }

    val showFab by remember(screenState) {
        derivedStateOf {
            !isLoading || conversations.isNotEmpty()
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
                            onAction(NavigationAction.NavigateToSettings)
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
                                Text(text = "Toggle list")
                            },
                            onClick = {
                                useLightList = !useLightList
                                dropDownMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(text = "Toggle only avatar placeholders")
                            },
                            onClick = {
                                showOnlyPlaceholders = !showOnlyPlaceholders
                                dropDownMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(text = "Toggle pull to refresh")
                            },
                            onClick = {
                                showPullRefresh = !showPullRefresh
                                dropDownMenuExpanded = false
                            }
                        )
                    }
                }
            }

            val title = @Composable {
                Row(modifier = Modifier) {
                    Text(
                        text = if (isLoading) "Loading..." else "Conversations",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            val toolbarColorAlpha by animateFloatAsState(
                targetValue = if (!listState.canScrollBackward) 1f else 0f,
                label = "toolbarColorAlpha",
                animationSpec = tween(durationMillis = 50)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = title,
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

                if (isLoading && conversations.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                modifier = Modifier.navigationBarsPadding(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = UiR.drawable.ic_baseline_create_24),
                        contentDescription = "Pencil icon"
                    )
                }
            }
        }
    ) { padding ->
        if (isLoading && conversations.isEmpty()) {
            Loader()
        } else {
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
                    if (showPullRefresh) {
                        Modifier
                            .fillMaxWidth()
                            .pullRefresh(pullRefreshState)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                }

                if (useLightList) {
                    LazyColumn(
                        state = listState,
                        modifier = listModifier
                    ) {
                        items(
                            count = conversations.size,
                            key = { index -> index }
                        ) { index ->
                            Text(
                                text = "Text #${index + 1}",
                                modifier = Modifier.height(64.dp)
                            )
                        }
                    }
                } else {
                    ConversationsListComposable(
                        onConversationsClick = {
                            onAction(
                                NavigationAction.NavigateToMessagesHistory(
                                    it.id
                                )
                            )
                        },
                        onConversationsLongClick = viewModel::onConversationItemLongClick,
                        screenState = screenState,
                        state = listState,
                        maxLines = maxLines,
                        showOnlyPlaceholders = showOnlyPlaceholders,
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
                                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationsListComposable(
    onConversationsClick: (UiConversation) -> Unit,
    onConversationsLongClick: (UiConversation) -> Unit,
    screenState: ConversationsScreenState,
    state: LazyListState,
    maxLines: Int,
    showOnlyPlaceholders: Boolean,
    modifier: Modifier,
    onOptionClicked: (UiConversation, ConversationOption) -> Unit,
    padding: PaddingValues
) {
    val conversations = screenState.conversations

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        itemsIndexed(
            items = conversations,
            key = { _, item -> item.id },
        ) { index, conversation ->

            val needToShowSpacer by remember {
                derivedStateOf {
                    index == 0
                }
            }

            if (needToShowSpacer) {
                Spacer(modifier = Modifier.height(padding.calculateTopPadding()))
            }

            val isUserAccount by remember(conversation) {
                derivedStateOf {
                    conversation.id == UserConfig.userId
                }
            }

            val options by remember(conversation) {
                derivedStateOf {
                    conversation.options
                }
            }

            ConversationItem(
                onItemClick = {
                    onConversationsClick(conversation)
                },
                onItemLongClick = {
                    onConversationsLongClick(conversation)
                },
                isUserAccount = isUserAccount,
                avatar = conversation.avatar,
                title = conversation.title.orDots(),
                message = conversation.message,
                date = conversation.date,
                maxLines = maxLines,
                isUnread = conversation.isUnread,
                isPinned = conversation.isPinned,
                isOnline = conversation.isOnline,
                isBirthday = conversation.isBirthday,
                attachmentImage = conversation.attachmentImage,
                isExpanded = conversation.isExpanded,
                unreadCount = conversation.unreadCount,
                interactionText = conversation.interactionText,
                showOnlyPlaceholders = showOnlyPlaceholders,
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                options = options,
                onOptionClicked = { option ->
                    onOptionClicked(conversation, option)
                }
            )

            val showDefaultSpacer by remember {
                derivedStateOf {
                    index < conversations.size - 1
                }
            }

            if (showDefaultSpacer) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            val showBottomNavigationBarsSpacer by remember {
                derivedStateOf {
                    index == conversations.size - 1
                }
            }

            if (showBottomNavigationBarsSpacer) {
                Spacer(modifier = Modifier.navigationBarsPadding())
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
        val conversationId = showOptions.showDeleteDialog
        DeleteDialog(
            conversationId = conversationId,
            viewModel = viewModel
        )
    }

    if (showOptions.showPinDialog != null) {
        val conversation = showOptions.showPinDialog
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

