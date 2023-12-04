package com.meloda.fast.screens.conversations.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
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
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.asUiText
import com.meloda.fast.ext.getString
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.getImage
import com.meloda.fast.screens.conversations.ConversationsViewModel
import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicatorDefaults
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import me.gingerninja.lazylist.hijacker.rememberLazyListStateHijacker
import org.koin.androidx.compose.koinViewModel

private val MinToolbarHeight = 96.dp
private val MaxToolbarHeight = 176.dp

@Composable
fun ConversationsRoute(
    navigateToMessagesHistory: (conversation: VkConversationUi) -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: ConversationsViewModel = koinViewModel<ConversationsViewModelImpl>()
) {
    val view = LocalView.current

    ConversationsScreenContent(
        onConversationsClick = navigateToMessagesHistory,
        onConversationsLongClick = viewModel::onConversationItemLongClick,
        onCreateChatClick = {
            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
        },
        onSettingsClick = navigateToSettings,
        viewModel = viewModel
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun ConversationsScreenContent(
    onConversationsClick: (VkConversationUi) -> Unit,
    onConversationsLongClick: (VkConversationUi) -> Unit,
    onCreateChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ConversationsViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val conversations = screenState.conversations

    val isLoading = screenState.isLoading

    // TODO: 01/12/2023, Danil Nikolaev: implement retrieving from settings
    val maxLines by remember {
        derivedStateOf {
            if (screenState.multilineEnabled) 2 else 1
        }
    }

    val listState = rememberLazyListState()

    // TODO: 26/11/2023, Danil Nikolaev: remove when fixed
    rememberLazyListStateHijacker(listState = listState)

    val topAppBarState = rememberTopAppBarState()
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

    var useLightList by remember {
        mutableStateOf(false)
    }
    var showOnlyPlaceholders by remember {
        mutableStateOf(false)
    }
    var showPullRefresh by remember {
        mutableStateOf(false)
    }


//    val toolbarHeightRange = with(LocalDensity.current) {
//        MinToolbarHeight.roundToPx()..MaxToolbarHeight.roundToPx()
//    }
//    val toolbarState = rememberToolbarState(toolbarHeightRange)
//
//    val nestedScrollConnection = remember {
//        object : NestedScrollConnection {
//            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//                toolbarState.scrollTopLimitReached =
//                    listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
//                toolbarState.scrollOffset = toolbarState.scrollOffset - available.y
//                return Offset(0f, toolbarState.consumed)
//            }
//        }
//    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
//            .nestedScroll(nestedScrollConnection)
        ,
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
                            onSettingsClick()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = "Settings")
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            viewModel.onRefresh()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = "Refresh")
                        }
                    )
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

            val title = @Composable {
                Row(modifier = Modifier) {
                    Text(
                        text = if (isLoading) "Loading..." else "Conversations",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {

                // TODO: 01/12/2023, Danil Nikolaev: implement scrolling by tutorial

                LargeTopAppBar(
                    title = title,
                    actions = actions,
//                    scrollBehavior = scrollBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
//                        .height(with(LocalDensity.current) { toolbarState.height.toDp() })
//                        .graphicsLayer { translationY = toolbarState.offset }
                )

                if (isLoading && conversations.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            if (!isLoading || conversations.isNotEmpty()) {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding(),
                    onClick = onCreateChatClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_create_24),
                        contentDescription = null
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
                    .padding(padding)
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
//                            .graphicsLayer {
//                                translationY = toolbarState.height + toolbarState.offset
//                            },
//                        contentPadding = PaddingValues(
//                            bottom = if (toolbarState is FixedScrollFlagState)
//                                MinToolbarHeight
//                            else 0.dp
//                        ),
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
                    ConversationsList(
                        onConversationsClick = onConversationsClick,
                        onConversationsLongClick = onConversationsLongClick,
                        conversations = conversations,
                        state = listState,
                        maxLines = maxLines,
                        showOnlyPlaceholders = showOnlyPlaceholders,
                        modifier = listModifier
                    )

                    if (showPullRefresh) {
                        PullRefreshIndicator(
                            refreshing = screenState.isLoading,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter),
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

@Composable
fun ConversationsList(
    onConversationsClick: (VkConversationUi) -> Unit,
    onConversationsLongClick: (VkConversationUi) -> Unit,
    conversations: List<VkConversationUi>,
    state: LazyListState,
    maxLines: Int,
    showOnlyPlaceholders: Boolean,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        items(
            count = conversations.size,
            key = { index ->
                conversations[index].conversationId
            }
        ) { index ->
            val conversation = conversations[index]
            val isUserAccount by remember {
                derivedStateOf {
                    conversation.conversationId == UserConfig.userId
                }
            }

            val avatar = conversation.avatar.getImage()
            val title by remember {
                derivedStateOf {
                    conversation.title.orDots()
                }
            }

            Conversation(
                onItemClick = {
                    onConversationsClick(conversation)
                },
                onItemLongClick = {
                    onConversationsLongClick(conversation)
                },
                isUserAccount = isUserAccount,
                avatar = avatar,
                title = title,
                message = conversation.message,
                date = conversation.date,
                maxLines = maxLines,
                isUnread = conversation.isUnread,
                isPinned = conversation.isPinned,
                isOnline = conversation.isOnline,
                isBirthday = conversation.isBirthday,
                attachmentImage = conversation.attachmentImage,
                unreadCount = conversation.unreadCount,
                interactionText = conversation.interactionText,
                showOnlyPlaceholders = showOnlyPlaceholders
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

    if (showOptions.showOptionsDialog != null) {
        val conversation = showOptions.showOptionsDialog
        OptionsDialog(
            screenState = screenState,
            conversation = conversation,
            viewModel = viewModel
        )
    }

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
fun OptionsDialog(
    screenState: ConversationsScreenState,
    conversation: VkConversationUi,
    viewModel: ConversationsViewModel
) {
    val conversationsSize = screenState.conversations.size
    val pinnedCount = screenState.pinnedConversationsCount

    var canPinOneMoreDialog = true
    if (conversationsSize > 4) {
        if (pinnedCount == 5 && !conversation.isPinned) {
            canPinOneMoreDialog = false
        }
    }

    val read = "Mark as read"

    val pin = stringResource(
        if (conversation.isPinned) R.string.conversation_context_action_unpin
        else R.string.conversation_context_action_pin
    )

    val delete = stringResource(R.string.conversation_context_action_delete)

    val params = mutableListOf<Pair<String, String>>()

    conversation.lastMessage?.run {
        if (conversation.isUnread && !this.isOut) {
            params += "read" to read
        }
    }

    if (canPinOneMoreDialog) params += "pin" to pin

    params += "delete" to delete

    val items = params.map { param ->
        UiText.Simple(param.second.asUiText().getString().orEmpty())
    }

    MaterialDialog(
        onDismissAction = viewModel::onOptionsDialogDismissed,
        items = items,
        onItemClick = { index ->
            val key = params[index].first
            viewModel.onOptionsDialogOptionClicked(conversation, key)
        }
    )
}

@Composable
fun DeleteDialog(
    conversationId: Int,
    viewModel: ConversationsViewModel
) {
    MaterialDialog(
        title = UiText.Resource(R.string.confirm_delete_conversation),
        positiveText = UiText.Resource(R.string.action_delete),
        positiveAction = { viewModel.onDeleteDialogPositiveClick(conversationId) },
        negativeText = UiText.Resource(R.string.cancel),
        onDismissAction = viewModel::onDeleteDialogDismissed
    )
}

@Composable
fun PinDialog(
    conversation: VkConversationUi,
    viewModel: ConversationsViewModel
) {
    MaterialDialog(
        title = UiText.Resource(
            if (conversation.isPinned) R.string.confirm_unpin_conversation
            else R.string.confirm_pin_conversation
        ),
        positiveText = UiText.Resource(
            if (conversation.isPinned) R.string.action_unpin
            else R.string.action_pin
        ),
        positiveAction = {
            viewModel.onPinDialogPositiveClick(conversation)
        },
        negativeText = UiText.Resource(R.string.cancel),
        onDismissAction = viewModel::onPinDialogDismissed
    )
}

