package com.meloda.fast.screens.messages.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.ext.unsafeLazy
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.screens.messages.MessagesHistoryViewModel
import com.meloda.fast.screens.messages.MessagesHistoryViewModelImpl
import com.meloda.fast.screens.messages.model.MessagesHistoryActionButtonMode
import com.meloda.fast.screens.messages.model.MessagesHistoryScreenState
import com.meloda.fast.screens.messages.screen.MessagesHistoryScreen
import com.meloda.fast.ui.AppTheme
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class MessagesHistoryFragment : BaseFragment() {

    private val messageText by unsafeLazy {
        context?.getString(R.string.message_input_hint).orEmpty()
    }

    private val viewModel: MessagesHistoryViewModel by viewModel<MessagesHistoryViewModelImpl>(
        parameters = { parametersOf(get<MessagesHistoryScreen>().getArguments()) }
    )

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
                MessagesHistoryScreen(
                    onBackClicked = { /*TODO*/ },
                    onAvatarClicked = { /*TODO*/ },
                    onAttachmentAddClick = { /*TODO*/ },
                    onTextInputChanged = viewModel::onInputChanged,
                    onEmojiClicked = {},
                    onEmojiLongClicked = {},
                    onSendClicked = { /*TODO*/ },
                    onSendLongClicked = { /*TODO*/ },
                    onMessageAvatarClicked = { /*TODO*/ },
                    onMessageAvatarLongClicked = { /*TODO*/ },
                    onTopBarMenuClick = viewModel::onTopAppBarMenuClicked,
                    screenState = screenState
                )
            }
        }
    }

    @Preview
    @Composable
    fun MessagesHistoryScreenPreview() {
        val screenState = MessagesHistoryScreenState(
            title = "PreviewTitle",
            avatar = UiImage.Resource(R.drawable.ic_account_circle_cut),
            messages = List(100) { index ->
                VkMessage(
                    id = index,
                    text = "Message #${index + 1}",
                    isOut = false,
                    peerId = 8637 * index,
                    fromId = 9728 * index,
                    date = 7193 * index,
                    randomId = 7862 * index,
                    action = null,
                    actionMemberId = null,
                    actionText = null,
                    actionConversationMessageId = null,
                    actionMessage = null,
                    updateTime = null,
                    important = false,
                    forwards = listOf(),
                    attachments = listOf(),
                    replyMessage = null,
                    geo = null
                )
            },
            message = "",
            attachments = emptyList(),
            isLoading = true,
            actionButtonMode = MessagesHistoryActionButtonMode.Record
        )

        AppTheme(
            useDarkTheme = true,
            useDynamicColors = false
        ) {
            MessagesHistoryScreen(
                onBackClicked = { /*TODO*/ },
                onAvatarClicked = { /*TODO*/ },
                onAttachmentAddClick = { /*TODO*/ },
                onTextInputChanged = {},
                onEmojiClicked = { /*TODO*/ },
                onEmojiLongClicked = { /*TODO*/ },
                onSendClicked = { /*TODO*/ },
                onSendLongClicked = { /*TODO*/ },
                onMessageAvatarClicked = { /*TODO*/ },
                onMessageAvatarLongClicked = { /*TODO*/ },
                onTopBarMenuClick = {},
                screenState = screenState
            )
        }
    }

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalLayoutApi::class,
    )
    @Composable
    fun MessagesHistoryScreen(
        onBackClicked: () -> Unit,
        onAvatarClicked: () -> Unit,
        onAttachmentAddClick: () -> Unit,
        onTextInputChanged: (String) -> Unit,
        onEmojiClicked: () -> Unit,
        onEmojiLongClicked: () -> Unit,
        onSendClicked: () -> Unit,
        onSendLongClicked: () -> Unit,
        onMessageAvatarClicked: () -> Unit,
        onMessageAvatarLongClicked: () -> Unit,
        onTopBarMenuClick: (id: Int) -> Unit,
        screenState: MessagesHistoryScreenState,
    ) {
        val title = screenState.title
        val avatar = screenState.avatar
        val messages = screenState.messages
        val message = screenState.message
        val attachments = screenState.attachments
        val isLoading = screenState.isLoading
        val actionButtonMode = screenState.actionButtonMode

        var wasEmpty by remember {
            mutableStateOf(true)
        }

        val lazyListState = rememberLazyListState()

        LaunchedEffect(messages) {
            if (messages.isEmpty()) return@LaunchedEffect
            if (wasEmpty) {
                lazyListState.scrollToItem(messages.lastIndex)
                wasEmpty = false
            }
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    state = lazyListState
                ) {
                    items(messages.size) { index ->
                        val message = messages[index]

                        if (index == 0) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            )
                            Spacer(
                                modifier = Modifier
                                    .height(64.dp)
                                    .fillMaxWidth()
                            )
                        }

                        Text(text = "Message: ${message.text.orEmpty()}")

                        if (index == messages.lastIndex) {
                            Spacer(modifier = Modifier.height(68.dp))
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .imePadding()
                            )
                        }
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
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
                                    Text(text = "Refresh")
                                }
                            )
                        }
                    }
                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { Text(text = title) },
                        navigationIcon = {
                            IconButton(onClick = onBackClicked) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_round_arrow_back_24),
                                    contentDescription = null
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ),
                        actions = actions
                    )

                    if (isLoading && messages.isNotEmpty()) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 60.dp)
                            .imeNestedScroll(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(onClick = onAttachmentAddClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_add_circle_outline_24),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var text by remember {
                                mutableStateOf(TextFieldValue(text = message))
                            }

                            TextField(
                                modifier = Modifier.weight(1f),
                                value = text,
                                onValueChange = { newText ->
                                    text = newText
                                    onTextInputChanged.invoke(newText.text)
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                ),
                                placeholder = { Text(text = messageText) }
                            )

                            IconButton(onClick = onEmojiClicked) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_outline_emoji_emotions_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(onClick = onSendClicked) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_round_mic_none_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                    }
                }

                if (isLoading && messages.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    companion object {
        fun newInstance(): MessagesHistoryFragment {
            return MessagesHistoryFragment()
        }
    }
}
