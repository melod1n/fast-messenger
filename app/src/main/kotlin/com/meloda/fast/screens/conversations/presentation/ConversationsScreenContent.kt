package com.meloda.fast.screens.conversations.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.common.collect.ImmutableList
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.conversations.ConversationsViewModel
import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import com.meloda.fast.screens.conversations.model.ConversationOption
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import com.meloda.fast.screens.settings.UserSettings
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicatorDefaults
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import me.gingerninja.lazylist.hijacker.rememberLazyListStateHijacker
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ConversationsRoute(
    navigateToMessagesHistory: (conversation: VkConversationUi) -> Unit,
    navigateToSettings: () -> Unit
) {
    val view = LocalView.current

    ConversationsScreenContent(
        onConversationsClick = navigateToMessagesHistory,
        onCreateChatClick = {
            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
        },
        onSettingsClick = navigateToSettings
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun ConversationsScreenContent(
    onConversationsClick: (VkConversationUi) -> Unit,
    onCreateChatClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val viewModel: ConversationsViewModel = koinViewModel<ConversationsViewModelImpl>()
    val userSettings: UserSettings = koinInject()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val conversations = screenState.conversations

    val isLoading = screenState.isLoading

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
                            onSettingsClick()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = R.string.title_settings))
                        }
                    )
                    DropdownMenuItem(
                        onClick = {
                            viewModel.onRefresh()
                            dropDownMenuExpanded = false
                        },
                        text = {
                            Text(text = stringResource(id = R.string.action_refresh))
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

            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = title,
                    actions = actions,
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .hazeChild(state = hazeState)
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
                FloatingActionButton(onClick = onCreateChatClick) {
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
                        onConversationsClick = onConversationsClick,
                        onConversationsLongClick = viewModel::onConversationItemLongClick,
                        screenState = screenState,
                        state = listState,
                        maxLines = maxLines,
                        showOnlyPlaceholders = showOnlyPlaceholders,
                        modifier = listModifier.then(
                            Modifier.haze(
                                state = hazeState,
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                blurRadius = 45.dp
                            )
                        ),
                        onOptionClicked = viewModel::onOptionClicked
                    )

                    AnimatedVisibility(
                        visible = showPullRefresh,
                        modifier = Modifier.fillMaxWidth()
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
    onConversationsClick: (VkConversationUi) -> Unit,
    onConversationsLongClick: (VkConversationUi) -> Unit,
    screenState: ConversationsScreenState,
    state: LazyListState,
    maxLines: Int,
    showOnlyPlaceholders: Boolean,
    modifier: Modifier,
    onOptionClicked: (VkConversationUi, ConversationOption) -> Unit,
) {
    val conversations = screenState.conversations

    LazyColumn(
        modifier = modifier,
        state = state
    ) {
        itemsIndexed(
            items = conversations,
            key = { _, item -> item.conversationId },
        ) { index, conversation ->
            val isUserAccount by remember(conversation) {
                derivedStateOf {
                    conversation.conversationId == UserConfig.userId
                }
            }

            val options by remember(conversation) {
                derivedStateOf {
                    ImmutableList.copyOf(conversation.options)
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
                modifier = Modifier.animateItemPlacement(),
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

