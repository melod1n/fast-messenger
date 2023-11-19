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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.imageLoader
import coil.request.ImageRequest
import com.meloda.fast.R
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.getImage
import com.meloda.fast.screens.conversations.ConversationsViewModel
import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationsRoute(
    navigateToMessagesHistory: (conversation: VkConversationUi) -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: ConversationsViewModel = koinViewModel<ConversationsViewModelImpl>()
) {
    val view = LocalView.current

    ConversationsScreen(
        onConversationsClick = navigateToMessagesHistory,
        onConversationsLongClick = viewModel::onConversationItemLongClick,
        onCreateChatClick = {
            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
        },
        onSettingsClick = navigateToSettings,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ConversationsScreen(
    onConversationsClick: (VkConversationUi) -> Unit,
    onConversationsLongClick: (VkConversationUi) -> Unit,
    onCreateChatClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ConversationsViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val conversations = screenState.conversations

    val avatars = remember {
        conversations.mapNotNull { conversation ->
            conversation.avatar.extractUrl()
        }
    }

    val isLoading = screenState.isLoading
    val multilineEnabled = screenState.multilineEnabled

    val lazyListState = rememberLazyListState()

    val scrollInProgress = remember {
        lazyListState.isScrollInProgress
    }

    println("is scrolling: $scrollInProgress")

    val pullRefreshState = rememberPullRefreshState(
        refreshing = screenState.isLoading,
        onRefresh = viewModel::onRefresh
    )

//    val isFirstPosition by remember {
//        derivedStateOf {
//            lazyListState.firstVisibleItemIndex == 0
//        }
//    }

//    val topAppBarContainerColor = if (isFirstPosition) {
//        MaterialTheme.colorScheme.background
//    } else {
//        MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
//    }

//    val topAppBarBackgroundAnimated by animateColorAsState(
//        targetValue = topAppBarContainerColor,
//        label = "topAppBarBackgroundAnimated",
//        animationSpec = spring(stiffness = Spring.StiffnessHigh)
//    )

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
                            Text(text = "Settings")
                        }
                    )
//                    DropdownMenuItem(
//                        onClick = {
//                            onRefreshClick()
//                            dropDownMenuExpanded = false
//                        },
//                        text = {
//                            Text(text = "Refresh")
//                        }
//                    )
                }
            }

            val title = @Composable {
                Row(modifier = Modifier) {
                    Text(text = if (isLoading) "Loading..." else "Conversations")
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = title,
                    actions = actions,
//                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topAppBarBackgroundAnimated)
                )

                if (isLoading && conversations.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            if (!isLoading || conversations.isNotEmpty()) {
//                AnimatedVisibility(
//                    visible = !scrollInProgress || conversations.isNotEmpty(),
//                    enter = slideIn(initialOffset = { IntOffset(x = 0, y = 300) }),
//                    exit = slideOut(targetOffset = { IntOffset(x = 0, y = 300) })
//                ) {
                FloatingActionButton(
                    modifier = Modifier.navigationBarsPadding(),
                    onClick = onCreateChatClick
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_create_24),
                        contentDescription = null
                    )
                }
//                }
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
                    .pullRefresh(pullRefreshState)
            ) {
                ConversationsList(
                    onConversationsClick = onConversationsClick,
                    onConversationsLongClick = onConversationsLongClick,
                    conversations = conversations,
                    state = lazyListState,
                    maxLines = if (multilineEnabled) 2 else 1,
                    avatarItems = avatars
                )

                PullRefreshIndicator(
                    refreshing = screenState.isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }

        }
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
    avatarItems: List<String>
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        avatarItems.forEach { avatar ->
            val request = ImageRequest.Builder(context)
                .data(avatar)
                .build()

            context.imageLoader.enqueue(request)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        state = state
    ) {
        items(
            count = conversations.size,
            key = { index ->
                val item = conversations[index]
                item.conversationId
            }
        ) { index ->
            val conversation = conversations[index]

            Conversation(
                onItemClick = {
                    onConversationsClick(conversation)
                },
                onItemLongClick = {
                    onConversationsLongClick(conversation)
                },
                id = conversation.id,
                avatar = conversation.avatar.getImage(),
                title = conversation.title.orDots(),
                message = conversation.message,
                date = conversation.date,
                maxLines = maxLines,
                isUnread = conversation.isUnread,
                isPinned = conversation.isPinned,
                isOnline = conversation.isOnline,
                isBirthday = conversation.isBirthday,
                interactionType = conversation.interactionType,
                interactiveUsers = conversation.interactiveUsers,
                peerType = conversation.peerType,
                attachmentImage = conversation.attachmentImage,
                unreadCount = conversation.unreadCount
            )

            if (index < conversations.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (index == conversations.size - 1) {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

/*
 @Composable
    fun HandleDialogs(screenState: ConversationsScreenState) {
        val showOptions = screenState.showOptions

        if (showOptions.showOptionsDialog != null) {
            val conversation = showOptions.showOptionsDialog
            OptionsDialog(screenState = screenState, conversation = conversation)
        }

        if (showOptions.showDeleteDialog != null) {
            val conversationId = showOptions.showDeleteDialog
            DeleteDialog(conversationId = conversationId)
        }

        if (showOptions.showPinDialog != null) {
            val conversation = showOptions.showPinDialog
            PinDialog(conversation = conversation)
        }
    }

    // TODO: 26.08.2023, Danil Nikolaev: remove usage of viewModel
    @Composable
    fun OptionsDialog(
        screenState: ConversationsScreenState,
        conversation: VkConversationUi
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

        val pin = string(
            if (conversation.isPinned) R.string.conversation_context_action_unpin
            else R.string.conversation_context_action_pin
        )

        val delete = string(R.string.conversation_context_action_delete)

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

    // TODO: 26.08.2023, Danil Nikolaev: remove usage of viewModel
    @Composable
    fun DeleteDialog(conversationId: Int) {
        MaterialDialog(
            title = UiText.Resource(R.string.confirm_delete_conversation),
            positiveText = UiText.Resource(R.string.action_delete),
            positiveAction = { viewModel.onDeleteDialogPositiveClick(conversationId) },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onDeleteDialogDismissed
        )
    }

    // TODO: 26.08.2023, Danil Nikolaev: remove usage of viewModel
    @Composable
    fun PinDialog(conversation: VkConversationUi) {
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
 */

