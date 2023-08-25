package com.meloda.fast.screens.conversations.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.asUiText
import com.meloda.fast.ext.getString
import com.meloda.fast.ext.string
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.conversations.ConversationsViewModel
import com.meloda.fast.screens.conversations.ConversationsViewModelImpl
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import com.meloda.fast.screens.conversations.model.ConversationsShowOptions
import com.meloda.fast.ui.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationsFragment : BaseFragment(R.layout.fragment_conversations) {

    private val viewModel: ConversationsViewModel by viewModel<ConversationsViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as? ComposeView)?.setContent {
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()

            AppTheme {
                ConversationsScreen(
                    onConversationsClick = viewModel::onConversationItemClick,
                    onConversationsLongClick = viewModel::onConversationItemLongClick,
                    onCreateChatClick = {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                    },
                    onTopBarMenuClick = viewModel::onToolbarMenuItemClicked,
                    screenState = screenState
                )

                HandleDialogs(screenState = screenState)
            }
        }
    }

    @Preview
    @Composable
    fun ConversationsScreenPreview() {
        val screenState = ConversationsScreenState(
            showOptions = ConversationsShowOptions.EMPTY,
            conversations = emptyList(),
            isLoading = true,
            useLargeTopAppBar = true,
            multilineEnabled = true,
            pinnedConversationsCount = 0
        )

        AppTheme {
            ConversationsScreen(
                onConversationsClick = {},
                onConversationsLongClick = {},
                onCreateChatClick = {},
                onTopBarMenuClick = {},
                screenState = screenState
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConversationsScreen(
        onConversationsClick: (VkConversationUi) -> Unit,
        onConversationsLongClick: (VkConversationUi) -> Unit,
        onCreateChatClick: () -> Unit,
        onTopBarMenuClick: (Int) -> Unit,
        screenState: ConversationsScreenState
    ) {
        val conversations = screenState.conversations
        val isLoading = screenState.isLoading
        val useLargeTopAppBar = screenState.useLargeTopAppBar
        val multilineEnabled = screenState.multilineEnabled

        val topAppBarState = rememberTopAppBarState()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState)

        val scaffoldModifier = if (useLargeTopAppBar) {
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        } else {
            Modifier.fillMaxSize()
        }

        val lazyListState = rememberLazyListState()


        Scaffold(
            modifier = scaffoldModifier,
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
                                onTopBarMenuClick.invoke(0)
                                dropDownMenuExpanded = false
                            },
                            text = {
                                Text(text = "Settings")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onTopBarMenuClick.invoke(1)
                                dropDownMenuExpanded = false
                            },
                            text = {
                                Text(text = "Refresh")
                            }
                        )
                    }
                }

                val title = @Composable {
                    Row(modifier = Modifier) {
                        Text(text = if (isLoading) "Loading..." else "Conversations")
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (useLargeTopAppBar) {
                        LargeTopAppBar(
                            title = title,
                            scrollBehavior = scrollBehavior,
                            actions = actions
                        )
                    } else {
                        TopAppBar(
                            title = title,
                            actions = actions
                        )
                    }

                    if (isLoading && conversations.isNotEmpty()) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            floatingActionButton = {
                if (!isLoading || conversations.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = !lazyListState.isScrollInProgress || conversations.isNotEmpty(),
                        enter = slideIn(initialOffset = { IntOffset(x = 0, y = 300) }),
                        exit = slideOut(targetOffset = { IntOffset(x = 0, y = 300) })
                    ) {
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
            }
        ) { padding ->
            if (isLoading && conversations.isEmpty()) {
                Loader()
            } else {
                ConversationsList(
                    onConversationsClick = onConversationsClick,
                    onConversationsLongClick = onConversationsLongClick,
                    conversations = conversations,
                    state = lazyListState,
                    padding = padding,
                    useMultiline = multilineEnabled,
                )
            }
        }
    }

    @Composable
    fun Loader() {
        AppTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    @Composable
    fun ConversationsList(
        onConversationsClick: (VkConversationUi) -> Unit,
        onConversationsLongClick: (VkConversationUi) -> Unit,
        conversations: List<VkConversationUi>,
        state: LazyListState,
        padding: PaddingValues,
        useMultiline: Boolean,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            state = state
        ) {
            items(
                count = conversations.size,
                key = { index ->
                    val item = conversations[index]
                    item.conversationId
                }
            ) { index ->
                Conversation(
                    onItemClick = onConversationsClick,
                    onItemLongClick = onConversationsLongClick,
                    conversation = conversations[index],
                    maxLines = if (useMultiline) 2 else 1,
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

            if (!this.text.isNullOrBlank()) {
                params += "share" to "Share"
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
}
