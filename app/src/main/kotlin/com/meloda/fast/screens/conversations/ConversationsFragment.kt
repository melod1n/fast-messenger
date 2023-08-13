package com.meloda.fast.screens.conversations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.asUiText
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.showDialog
import com.meloda.fast.ext.string
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.SettingsFragment
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
        listenViewModel()

        (view as? ComposeView)?.setContent {
            ConversationsScreen()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConversationsScreen() {
        val conversations by viewModel.conversationsList.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

        val useLargeTopAppBar = AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_USE_LARGE_TOP_APP_BAR,
            SettingsFragment.DEFAULT_VALUE_USE_LARGE_TOP_APP_BAR
        )
        val useMultiline = AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_APPEARANCE_MULTILINE,
            SettingsFragment.DEFAULT_VALUE_MULTILINE
        )
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

        AppTheme {
            Scaffold(
                modifier = scaffoldModifier,
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
                            expanded = dropDownMenuExpanded,
                            onDismissRequest = {
                                dropDownMenuExpanded = false
                            },
                            offset = DpOffset(x = 0.dp, y = (-60).dp)
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    viewModel.onToolbarMenuItemClicked(R.id.settings)
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
                        }
                    }

                    val title = @Composable {
                        Text(text = if (isLoading) "Loading..." else "Conversations")
                    }

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
                },
                floatingActionButton = {
                    if (!isLoading || conversations.isNotEmpty()) {
                        AnimatedVisibility(
                            visible = !lazyListState.isScrollInProgress || conversations.isNotEmpty(),
                            enter = slideIn(initialOffset = { IntOffset(x = 0, y = 300) }),
                            exit = slideOut(targetOffset = { IntOffset(x = 0, y = 300) })
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    view?.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                                }
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
                        conversations = conversations,
                        padding = padding,
                        state = lazyListState,
                        useMultiline = useMultiline,
                    )
                }
            }
        }
    }

    @Composable
    fun Loader() {
        AppTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    @Composable
    fun ConversationsList(
        conversations: List<VkConversationUi>,
        padding: PaddingValues,
        state: LazyListState,
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
                    onItemClick = viewModel::onConversationItemClick,
                    onItemLongClick = viewModel::onConversationItemLongClick,
                    conversation = conversations[index],
                    maxLines = if (useMultiline) 2 else 1,
                )

                if (index < conversations.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // TODO: 05.08.2023, Danil Nikolaev: remove and use compose dialogs
    private fun listenViewModel() = with(viewModel) {
        isNeedToShowOptionsDialog.listenValue(::showOptionsDialog)
        isNeedToShowDeleteDialog.listenValue(::showDeleteConversationDialog)
        isNeedToShowPinDialog.listenValue(::showPinConversationDialog)
    }

    // TODO: 06.04.2023, Danil Nikolaev: extract creating options to VM
    private fun showOptionsDialog(conversation: VkConversationUi?) {
        if (conversation == null) return

        var canPinOneMoreDialog = true
        if (viewModel.conversationsList.value.size > 4) {
            if (viewModel.pinnedConversationsCount.value == 5 && !conversation.isPinned) {
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
            if (!conversation.isUnread && !this.isOut) {
                params += "read" to read
            }

            if (!this.text.isNullOrBlank()) {
                params += "share" to "Share"
            }
        }

        if (canPinOneMoreDialog) params += "pin" to pin

        params += "delete" to delete

        context?.showDialog(
            items = params.map { param -> param.second.asUiText() },
            itemsClickAction = { index, _ ->
                val key = params[index].first
                viewModel.onOptionsDialogOptionClicked(conversation, key)
            },
            onDismissAction = viewModel::onOptionsDialogDismissed
        )
    }

    private fun showDeleteConversationDialog(conversationId: Int?) {
        if (conversationId == null) return

        context?.showDialog(
            title = UiText.Resource(R.string.confirm_delete_conversation),
            positiveText = UiText.Resource(R.string.action_delete),
            positiveAction = { viewModel.onDeleteDialogPositiveClick(conversationId) },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onDeleteDialogDismissed
        )
    }

    private fun showPinConversationDialog(conversation: VkConversationUi?) {
        if (conversation == null) return

        context?.showDialog(
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
