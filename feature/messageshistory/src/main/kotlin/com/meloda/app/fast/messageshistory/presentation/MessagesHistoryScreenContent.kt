package com.meloda.app.fast.messageshistory.presentation

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModel
import com.meloda.app.fast.messageshistory.model.ActionMode
import com.meloda.app.fast.messageshistory.model.UiAction
import me.gingerninja.lazylist.hijacker.rememberLazyListStateHijacker
import com.meloda.app.fast.designsystem.R as UiR

typealias OnAction = (UiAction) -> Unit

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun MessagesHistoryScreenContent(
    onAction: OnAction,
    viewModel: MessagesHistoryViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.isNeedToOpenChatMaterials) {
        viewModel.onChatMaterialsOpened()
        onAction(UiAction.OpenChatMaterials)
    }

    val messages = screenState.messages

    var wasEmpty by remember {
        mutableStateOf(true)
    }

    val lazyListState = rememberLazyListState()

    // TODO: 26/11/2023, Danil Nikolaev: remove when fixed
    rememberLazyListStateHijacker(listState = lazyListState)

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
                                viewModel.onTopAppBarMenuClicked(0)
                                dropDownMenuExpanded = false
                            },
                            text = {
                                Text(text = "Refresh")
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                viewModel.onTopAppBarMenuClicked(1)
                                dropDownMenuExpanded = false
                            },
                            text = {
                                Text(text = "Materials")
                            }
                        )
                    }
                }
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text(text = screenState.title) },
                    navigationIcon = {
                        IconButton(onClick = { onAction(UiAction.BackClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back button"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ),
                    actions = actions
                )

                screenState.status?.let { status ->
                    Text(
                        text = status,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    )
                }

                if (screenState.isLoading && messages.isNotEmpty()) {
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

                    IconButton(onClick = viewModel::onAttachmentButtonClicked) {
                        Icon(
                            painter = painterResource(id = UiR.drawable.ic_round_add_circle_outline_24),
                            contentDescription = "Add attachment button",
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
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = screenState.message,
                            onValueChange = viewModel::onInputChanged,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                            ),
                            placeholder = { Text(text = stringResource(id = UiR.string.message_input_hint)) }
                        )

                        IconButton(onClick = viewModel::onEmojiButtonClicked) {
                            Icon(
                                painter = painterResource(id = UiR.drawable.ic_outline_emoji_emotions_24),
                                contentDescription = "Emoji button",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = viewModel::onActionButtonClicked) {
                            Icon(
                                painter = painterResource(
                                    id = when (screenState.actionMode) {
                                        ActionMode.Delete -> UiR.drawable.round_delete_outline_24
                                        ActionMode.Edit -> UiR.drawable.ic_round_done_24
                                        ActionMode.Record -> UiR.drawable.ic_round_mic_none_24
                                        ActionMode.Send -> UiR.drawable.round_send_24
                                    }
                                ),
                                contentDescription = when (screenState.actionMode) {
                                    ActionMode.Delete -> "Delete message button"
                                    ActionMode.Edit -> "Edit message button"
                                    ActionMode.Record -> "Record audio message button"
                                    ActionMode.Send -> "Send message button"
                                },
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }

            if (screenState.isLoading && messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
