package dev.meloda.fast.messageshistory.presentation

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.messageshistory.MessagesHistoryViewModel
import dev.meloda.fast.messageshistory.MessagesHistoryViewModelImpl
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessageDialog
import dev.meloda.fast.messageshistory.model.MessageOption
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.messageshistory.util.firstMessage
import dev.meloda.fast.messageshistory.util.indexOfMessageByCmId
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.IconButton
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList
import dev.meloda.fast.ui.util.getImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.util.concurrent.TimeUnit
import dev.meloda.fast.ui.R as UiR

@Composable
fun MessagesHistoryRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onChatMaterialsDropdownItemClicked: (peerid: Long, conversationMessageid: Long) -> Unit,
    viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val uiMessages by viewModel.uiMessages.collectAsStateWithLifecycle()
    val messageDialog by viewModel.messageDialog.collectAsStateWithLifecycle()
    val selectedMessages by viewModel.selectedMessages.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()
    val scrollIndex by viewModel.isNeedToScrollToIndex.collectAsStateWithLifecycle()

    val userSettings: UserSettings = koinInject()
    val showEmojiButton by userSettings.showEmojiButton.collectAsStateWithLifecycle()

    MessagesHistoryScreen(
        screenState = screenState,
        messages = messages.toImmutableList(),
        uiMessages = uiMessages.toImmutableList(),
        scrollIndex = scrollIndex,
        selectedMessages = selectedMessages.toImmutableList(),
        baseError = baseError,
        canPaginate = canPaginate,
        showEmojiButton = showEmojiButton,
        onBack = onBack,
        onClose = viewModel::onCloseButtonClicked,
        onScrolledToIndex = viewModel::onScrolledToIndex,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onChatMaterialsDropdownItemClicked = onChatMaterialsDropdownItemClicked,
        onRefresh = viewModel::onRefresh,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onMessageInputChanged = viewModel::onMessageInputChanged,
        onAttachmentButtonClicked = viewModel::onAttachmentButtonClicked,
        onActionButtonClicked = viewModel::onActionButtonClicked,
        onEmojiButtonLongClicked = viewModel::onEmojiButtonLongClicked,
        onMessageClicked = viewModel::onMessageClicked,
        onMessageLongClicked = viewModel::onMessageLongClicked,
        onPinnedMessageClicked = viewModel::onPinnedMessageClicked,
        onUnpinMessageButtonClicked = viewModel::onUnpinMessageClicked,
        onDeleteSelectedButtonClicked = viewModel::onDeleteSelectedMessagesClicked
    )

    HandleDialogs(
        screenState = screenState,
        messageDialog = messageDialog,
        onConfirmed = viewModel::onDialogConfirmed,
        onDismissed = viewModel::onDialogDismissed,
        onItemPicked = viewModel::onDialogItemPicked
    )
}

@Composable
fun HandleDialogs(
    screenState: MessagesHistoryScreenState,
    messageDialog: MessageDialog?,
    onConfirmed: (MessageDialog, Bundle) -> Unit = { _, _ -> },
    onDismissed: (MessageDialog) -> Unit = {},
    onItemPicked: (MessageDialog, Bundle) -> Unit = { _, _ -> }
) {
    when (messageDialog) {
        null -> Unit

        is MessageDialog.MessageOptions -> {
            MessageOptionsDialog(
                screenState = screenState,
                message = messageDialog.message,
                onDismissed = { onDismissed(messageDialog) },
                onItemPicked = { bundle -> onItemPicked(messageDialog, bundle) }
            )
        }

        is MessageDialog.MessageDelete -> {
            MessageDeleteDialog(
                messages = listOf(messageDialog.message),
                onConfirmed = { onConfirmed(messageDialog, it) },
                onDismissed = { onDismissed(messageDialog) }
            )
        }

        is MessageDialog.MessagesDelete -> {
            MessageDeleteDialog(
                messages = messageDialog.messages,
                onConfirmed = { onConfirmed(messageDialog, it) },
                onDismissed = { onDismissed(messageDialog) }
            )
        }

        is MessageDialog.MessagePin,
        is MessageDialog.MessageUnpin -> {
            MessagePinStateDialog(
                pin = messageDialog is MessageDialog.MessagePin,
                onConfirmed = { onConfirmed(messageDialog, bundleOf()) },
                onDismissed = { onDismissed(messageDialog) }
            )
        }

        is MessageDialog.MessageMarkImportance -> {
            MessageImportanceDialog(
                important = messageDialog.isImportant,
                onConfirmed = { onConfirmed(messageDialog, bundleOf()) },
                onDismissed = { onDismissed(messageDialog) }
            )
        }

        is MessageDialog.MessageSpam -> {
            MessageSpamDialog(
                spam = messageDialog.isSpam,
                onConfirmed = { onConfirmed(messageDialog, bundleOf()) },
                onDismissed = { onDismissed(messageDialog) }
            )
        }
    }
}


@Composable
fun MessageOptionsDialog(
    screenState: MessagesHistoryScreenState,
    message: VkMessage,
    onDismissed: () -> Unit = {},
    onItemPicked: (Bundle) -> Unit
) {
    val options = mutableListOf<MessageOption>()
    if (message.isFailed()) {
        options += MessageOption.Retry
    } else {
        options += MessageOption.Reply
        options += MessageOption.ForwardHere
        options += MessageOption.Forward

        if (message.isPeerChat() && screenState.conversation.canChangePin) {
            options += if (message.isPinned) MessageOption.Unpin else MessageOption.Pin
        }

        if (!message.isRead(screenState.conversation)) {
            options += MessageOption.Read
        }

        options += MessageOption.Copy

        if (message.isOut) {
            val diff = System.currentTimeMillis() - message.date * 1000L
            if (diff - TimeUnit.DAYS.toMillis(1) <= 0) {
                options += MessageOption.Edit
            }
        }

        options += if (message.isImportant) MessageOption.UnmarkAsImportant
        else MessageOption.MarkAsImportant


        if (!message.isOut) {
            options += if (message.isSpam) MessageOption.UnmarkAsSpam
            else MessageOption.MarkAsSpam
        }
    }

    options += MessageOption.Delete

    val messageOptions = options.map { option ->
        Triple(
            stringResource(option.titleResId),
            painterResource(option.iconResId),
            when {
                option in listOf(
                    MessageOption.Delete,
                    MessageOption.MarkAsSpam
                ) -> MaterialTheme.colorScheme.error

                else -> MaterialTheme.colorScheme.primary
            }
        )
    }

    MaterialDialog(onDismissRequest = onDismissed) {
        messageOptions
            .forEachIndexed { index, (title, painter, tintColor) ->
                DropdownMenuItem(
                    text = {
                        Row {
                            Text(text = title)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    leadingIcon = {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painter,
                                contentDescription = null,
                                tint = tintColor
                            )
                        }
                    },
                    onClick = {
                        onDismissed()
                        val pickedOption = options[index]
                        onItemPicked(bundleOf("option" to pickedOption))
                    }
                )
            }
    }
}

@Composable
fun MessageDeleteDialog(
    messages: List<VkMessage>,
    onConfirmed: (Bundle) -> Unit = {},
    onDismissed: () -> Unit = {},
) {
    var forEveryone by remember {
        mutableStateOf(
            !messages.any { it.peerId == UserConfig.userId }
                    && messages.all(VkMessage::isOut)
        )
    }

    val shouldBeDisabled by remember(messages) {
        mutableStateOf(
            messages.any { it.peerId == UserConfig.userId }
                    || messages.all(VkMessage::isFailed)
                    || !messages.all(VkMessage::isOut)
        )
    }

    MaterialDialog(
        onDismissRequest = onDismissed,
        title = stringResource(UiR.string.delete_message_title),
        confirmText = stringResource(UiR.string.action_delete),
        confirmAction = {
            onConfirmed(
                bundleOf("everyone" to if (messages.all(VkMessage::isOut)) forEveryone else false)
            )
        },
        cancelText = stringResource(UiR.string.cancel),
    ) {
        Row(
            modifier = Modifier
                .then(
                    if (!shouldBeDisabled) {
                        Modifier.clickable { forEveryone = !forEveryone }
                    } else Modifier)
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .padding(start = 24.dp, end = 16.dp)
        ) {
            Checkbox(
                checked = forEveryone,
                onCheckedChange = null,
                enabled = !shouldBeDisabled
            )

            Spacer(modifier = Modifier.width(8.dp))

            LocalContentAlpha(
                alpha = if (shouldBeDisabled) ContentAlpha.disabled
                else ContentAlpha.high
            ) {
                Text(text = stringResource(UiR.string.delete_message_for_everyone))
            }
        }
    }
}

@Composable
fun MessagePinStateDialog(
    pin: Boolean,
    onConfirmed: () -> Unit = {},
    onDismissed: () -> Unit = {},
) {
    MaterialDialog(
        onDismissRequest = onDismissed,
        title = stringResource(
            if (pin) UiR.string.pin_message_title
            else UiR.string.unpin_message_title
        ),
        text = stringResource(
            if (pin) UiR.string.pin_message_text
            else UiR.string.unpin_message_text
        ),
        confirmText = stringResource(
            if (pin) UiR.string.action_pin
            else UiR.string.action_unpin
        ),
        confirmAction = onConfirmed,
        cancelText = stringResource(UiR.string.cancel)
    )
}

@Composable
fun MessageImportanceDialog(
    important: Boolean,
    onConfirmed: () -> Unit = {},
    onDismissed: () -> Unit = {},
) {
    MaterialDialog(
        onDismissRequest = onDismissed,
        title = stringResource(
            if (important) UiR.string.important_message_title
            else UiR.string.unimportant_message_title
        ),
        text = stringResource(
            if (important) UiR.string.important_message_text
            else UiR.string.unimportant_message_text
        ),
        confirmText = stringResource(
            if (important) UiR.string.action_mark
            else UiR.string.action_unmark
        ),
        confirmAction = onConfirmed,
        cancelText = stringResource(UiR.string.cancel)
    )
}

@Composable
fun MessageSpamDialog(
    spam: Boolean,
    onConfirmed: () -> Unit = {},
    onDismissed: () -> Unit = {},
) {
    MaterialDialog(
        onDismissRequest = onDismissed,
        title = stringResource(
            if (spam) UiR.string.spam_message_title
            else UiR.string.unspam_message_title
        ),
        text = stringResource(
            if (spam) UiR.string.spam_message_text
            else UiR.string.unspam_message_text
        ),
        confirmText = stringResource(
            if (spam) UiR.string.action_mark
            else UiR.string.action_unmark
        ),
        confirmAction = onConfirmed,
        cancelText = stringResource(UiR.string.cancel)
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun MessagesHistoryScreen(
    screenState: MessagesHistoryScreenState = MessagesHistoryScreenState.EMPTY,
    messages: ImmutableList<VkMessage> = emptyImmutableList(),
    uiMessages: ImmutableList<UiItem> = emptyImmutableList(),
    scrollIndex: Int? = null,
    selectedMessages: ImmutableList<VkMessage> = emptyImmutableList(),
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    showEmojiButton: Boolean = false,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    onScrolledToIndex: () -> Unit = {},
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onChatMaterialsDropdownItemClicked: (peerid: Long, conversationMessageid: Long) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onMessageInputChanged: (TextFieldValue) -> Unit = {},
    onAttachmentButtonClicked: () -> Unit = {},
    onActionButtonClicked: () -> Unit = {},
    onEmojiButtonLongClicked: () -> Unit = {},
    onMessageClicked: (Long) -> Unit = {},
    onMessageLongClicked: (Long) -> Unit = {},
    onPinnedMessageClicked: (Long) -> Unit = {},
    onUnpinMessageButtonClicked: () -> Unit = {},
    onDeleteSelectedButtonClicked: () -> Unit = {}
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val theme = LocalThemeConfig.current
    val listState = rememberLazyListState()
    val hazeState = remember { HazeState() }

    LaunchedEffect(scrollIndex) {
        if (scrollIndex != null) {
            coroutineScope.launch {
                listState.animateScrollToItem(scrollIndex)
                onScrolledToIndex()
            }
        }
    }

    BackHandler(
        enabled = selectedMessages.isNotEmpty(),
        onBack = onClose
    )

    val pinnedMessage by remember(screenState) {
        derivedStateOf {
            screenState.conversation.pinnedMessage
        }
    }


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

    var dropDownMenuExpanded by remember {
        mutableStateOf(false)
    }

    val topBarContainerColorAlpha by animateFloatAsState(
        targetValue = if (!theme.enableBlur || !listState.canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val topBarContainerColor by animateColorAsState(
        targetValue =
            if (theme.enableBlur || !listState.canScrollBackward) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    var messageBarHeight by remember {
        mutableStateOf(0.dp)
    }

    val density = LocalDensity.current

    val showReplyAction by remember(selectedMessages) {
        derivedStateOf { selectedMessages.size == 1 }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarContainerColor.copy(alpha = topBarContainerColorAlpha))
                    .then(
                        if (theme.enableBlur) {
                            Modifier.hazeEffect(
                                state = hazeState,
                                style = HazeMaterials.thick()
                            )
                        } else Modifier
                    )
            ) {
                TopAppBar(
                    modifier = Modifier
                        .then(
                            if (theme.enableBlur) {
                                Modifier.hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.thick()
                                )
                            } else Modifier
                        )
                        .fillMaxWidth(),
                    title = {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedMessages.isEmpty()) {
                                val avatar = screenState.avatar.getImage()
                                if (avatar is Painter) {
                                    Image(
                                        painter = avatar,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    AsyncImage(
                                        model = screenState.avatar.getImage(),
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        placeholder = painterResource(id = UiR.drawable.ic_account_circle_cut),
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Text(
                                text = when {
                                    screenState.isLoading -> stringResource(id = UiR.string.title_loading)
                                    selectedMessages.size > 0 -> "(${selectedMessages.size})"
                                    else -> screenState.title
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (selectedMessages.isEmpty()) onBack()
                                else onClose()
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedMessages.isEmpty()) {
                                    Icons.AutoMirrored.Rounded.ArrowBack
                                } else {
                                    Icons.Rounded.Close
                                },
                                contentDescription = "Back button"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        if (selectedMessages.isNotEmpty()) {
                            AnimatedVisibility(showReplyAction) {
                                IconButton(
                                    onClick = {
                                        if (AppSettings.General.enableHaptic) {
                                            view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(UiR.drawable.round_reply_24),
                                        contentDescription = null
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (AppSettings.General.enableHaptic) {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(UiR.drawable.round_reply_all_24),
                                    contentDescription = null
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (AppSettings.General.enableHaptic) {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(UiR.drawable.round_forward_24),
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = onDeleteSelectedButtonClicked) {
                                Icon(
                                    painter = painterResource(UiR.drawable.round_delete_outline_24),
                                    contentDescription = null
                                )
                            }
                        } else {
                            if (screenState.isLoading) {
                                return@TopAppBar
                            }
                            IconButton(
                                onClick = { dropDownMenuExpanded = true }
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
                                offset = DpOffset(x = (-4).dp, y = (-60).dp)
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        dropDownMenuExpanded = false

                                        // TODO: 11/07/2024, Danil Nikolaev: to VM

                                        // TODO: 23-Mar-25, Danil Nikolaev: crash if no messages (ex. new chat)
                                        onChatMaterialsDropdownItemClicked(
                                            screenState.conversationId,
                                            uiMessages.values.firstMessage().conversationMessageId
                                        )
                                    },
                                    text = {
                                        Text(text = stringResource(UiR.string.chat_materials_action_title))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(UiR.drawable.ic_multimedia),
                                            contentDescription = null
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        onRefresh()
                                        dropDownMenuExpanded = false
                                    },
                                    text = {
                                        Text(text = stringResource(UiR.string.action_refresh))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Refresh,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                )

                val showHorizontalProgressBar by remember(screenState) {
                    derivedStateOf { screenState.isLoading && messages.isNotEmpty() }
                }
                if (showHorizontalProgressBar) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                AnimatedVisibility(!showHorizontalProgressBar) {
                    HorizontalDivider()
                }

                if (!screenState.isLoading && pinnedMessage != null) {
                    PinnedMessageContainer(
                        modifier = Modifier,
                        pinnedMessage = requireNotNull(pinnedMessage),
                        title = screenState.pinnedTitle.orDots(),
                        summary = screenState.pinnedSummary,
                        canChangePin = screenState.conversation.canChangePin,
                        onPinnedMessageClicked = onPinnedMessageClicked,
                        onUnpinMessageButtonClicked = onUnpinMessageButtonClicked
                    )
                    HorizontalDivider()
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = padding.calculateStartPadding(LayoutDirection.Ltr))
                .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr))
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            MessagesList(
                modifier = Modifier.align(Alignment.BottomStart),
                hazeState = hazeState,
                listState = listState,
                hasPinnedMessage = pinnedMessage != null,
                uiMessages = uiMessages,
                isPaginating = screenState.isPaginating,
                messageBarHeight = messageBarHeight,
                onRequestScrollToCmId = { cmId ->
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = uiMessages.values.indexOfMessageByCmId(cmId)
                        )
                    }
                },
                onMessageClicked = { id ->
                    if (selectedMessages.isNotEmpty()) {
                        if (AppSettings.General.enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                        }
                    }
                    onMessageClicked(id)
                },
                onMessageLongClicked = onMessageLongClicked
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Transparent)
                    .padding(bottom = 8.dp)
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(36.dp))
                            .then(
                                if (theme.enableBlur) {
                                    Modifier.hazeEffect(
                                        state = hazeState,
                                        style = HazeMaterials.ultraThin()
                                    ).border(1.dp, MaterialTheme.colorScheme.outlineVariant,
                                        RoundedCornerShape(36.dp)
                                    )
                                } else Modifier
                            )
                            .animateContentSize()
                            .weight(1f)
                            .background(
                                if (theme.enableBlur) Color.Transparent
                                else MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                            )
                            .onGloballyPositioned {
                                messageBarHeight = with(density) {
                                    it.size.height.toDp()
                                }
                            },
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))

                        if (showEmojiButton) {
                            val scope = rememberCoroutineScope()
                            val rotation = remember { Animatable(0f) }

                            Column(verticalArrangement = Arrangement.Bottom) {
                                IconButton(
                                    onClick = {
                                        if (AppSettings.General.enableHaptic) {
                                            view.performHapticFeedback(
                                                HapticFeedbackConstantsCompat.REJECT
                                            )
                                        }
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
                                    onLongClick = {
                                        if (AppSettings.General.enableHaptic) {
                                            view.performHapticFeedback(
                                                HapticFeedbackConstantsCompat.LONG_PRESS
                                            )
                                        }
                                        onEmojiButtonLongClicked()
                                    },
                                    modifier = Modifier.rotate(rotation.value)
                                ) {
                                    Icon(
                                        painter = painterResource(id = UiR.drawable.ic_outline_emoji_emotions_24),
                                        contentDescription = "Emoji button",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        TextField(
                            modifier = Modifier.weight(1f),
                            value = screenState.message,
                            onValueChange = onMessageInputChanged,
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                            ),
                            placeholder = {
                                Text(
                                    text = stringResource(id = UiR.string.message_input_hint),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )

                        val scope = rememberCoroutineScope()
                        val attachmentRotation = remember { Animatable(0f) }

                        Column(verticalArrangement = Arrangement.Bottom) {
                            IconButton(
                                onClick = {
                                    onAttachmentButtonClicked()
                                    if (AppSettings.General.enableHaptic) {
                                        view.performHapticFeedback(
                                            HapticFeedbackConstantsCompat.REJECT
                                        )
                                    }
                                    scope.launch {
                                        for (i in 20 downTo 0 step 4) {
                                            attachmentRotation.animateTo(
                                                targetValue = i.toFloat(),
                                                animationSpec = tween(50)
                                            )
                                            if (i > 0) {
                                                attachmentRotation.animateTo(
                                                    targetValue = -i.toFloat(),
                                                    animationSpec = tween(50)
                                                )
                                            }
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = UiR.drawable.round_attach_file_24),
                                    contentDescription = "Add attachment button",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.rotate(30f + attachmentRotation.value)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        val micRotation = remember { Animatable(0f) }

                        Column(verticalArrangement = Arrangement.Bottom) {
                            IconButton(
                                onClick = {
                                    if (screenState.actionMode == ActionMode.Record) {
                                        if (AppSettings.General.enableHaptic) {
                                            view.performHapticFeedback(
                                                HapticFeedbackConstantsCompat.REJECT
                                            )
                                        }
                                        scope.launch {
                                            for (i in 20 downTo 0 step 4) {
                                                micRotation.animateTo(
                                                    targetValue = i.toFloat(),
                                                    animationSpec = tween(50)
                                                )
                                                if (i > 0) {
                                                    micRotation.animateTo(
                                                        targetValue = -i.toFloat(),
                                                        animationSpec = tween(50)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        onActionButtonClicked()
                                    }
                                },
                                modifier = Modifier.rotate(micRotation.value)
                            ) {
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

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }

            when {
                screenState.isLoading && messages.values.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                baseError != null -> {
                    VkErrorView(baseError = baseError)
                }
            }
        }
    }
}
