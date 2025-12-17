package dev.meloda.fast.messageshistory.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.domain.util.indexOfMessageByCmId
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.Loader
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.model.vk.MessageUiItem
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalHazeMaterialsApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
fun MessagesHistoryScreen(
    screenState: MessagesHistoryScreenState = MessagesHistoryScreenState.EMPTY,
    messages: ImmutableList<VkMessage> = emptyImmutableList(),
    uiMessages: ImmutableList<MessageUiItem> = emptyImmutableList(),
    isSelectedAtLeastOne: Boolean = false,
    scrollIndex: Int? = null,
    selectedMessages: ImmutableList<VkMessage> = emptyImmutableList(),
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    showEmojiButton: Boolean = false,
    showAttachmentButton: Boolean = false,
    inputFieldFocusRequester: Boolean,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    onScrolledToIndex: () -> Unit = {},
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onTopBarClicked: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onMessageInputChanged: (TextFieldValue) -> Unit = {},
    onAttachmentButtonClicked: () -> Unit = {},
    onActionButtonClicked: () -> Unit = {},
    onEmojiButtonLongClicked: () -> Unit = {},
    onMessageClicked: (Long) -> Unit = {},
    onMessageLongClicked: (Long) -> Unit = {},
    onPhotoClicked: (images: List<String>, index: Int) -> Unit = { _, _ -> },
    onPinnedMessageClicked: (Long) -> Unit = {},
    onUnpinMessageButtonClicked: () -> Unit = {},
    onDeleteSelectedButtonClicked: () -> Unit = {},
    onBoldRequested: () -> Unit = {},
    onItalicRequested: () -> Unit = {},
    onLinkRequested: () -> Unit = {},
    onUnderlineRequested: () -> Unit = {},
    onRegularRequested: () -> Unit = {},
    onReplyCloseClicked: () -> Unit = {},
    onRequestReplyToMessage: (cmId: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val theme = LocalThemeConfig.current
    val listState = rememberLazyListState()
    val hazeState = remember { HazeState(true) }

    val currentOnMessageClicked by rememberUpdatedState(onMessageClicked)

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

    val pinnedMessage = screenState.pinnedMessage

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

    val showReplyAction by remember(selectedMessages) {
        derivedStateOf { selectedMessages.size == 1 }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            val topBarTitle by remember(screenState, selectedMessages) {
                derivedStateOf {
                    when {
                        screenState.isLoading -> context.getString(R.string.title_loading)
                        selectedMessages.isNotEmpty() -> "(${selectedMessages.size})"
                        else -> screenState.title
                    }
                }
            }

            MessagesHistoryTopBarContainer(
                hazeState = hazeState,
                showReplyAction = showReplyAction,
                topBarContainerColor = topBarContainerColor,
                topBarContainerColorAlpha = topBarContainerColorAlpha,
                isClickable = !(screenState.isLoading && messages.isEmpty()),
                isMessagesSelecting = selectedMessages.isNotEmpty(),
                isPeerAccount = screenState.convoId == UserConfig.userId,
                avatar = screenState.avatar,
                title = topBarTitle,
                showHorizontalProgressBar = screenState.isLoading && messages.isNotEmpty(),
                showPinnedContainer = !screenState.isLoading && pinnedMessage != null,
                pinnedMessage = pinnedMessage,
                pinnedTitle = screenState.pinnedTitle,
                pinnedSummary = screenState.pinnedSummary,
                showUnpinButton = screenState.convo.canChangePin,
                onTopBarClicked = onTopBarClicked,
                onBack = onBack,
                onClose = onClose,
                onDeleteSelectedButtonClicked = onDeleteSelectedButtonClicked,
                onRefresh = onRefresh,
                onPinnedMessageClicked = onPinnedMessageClicked,
                onUnpinMessageButtonClicked = onUnpinMessageButtonClicked
            )
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
                isSelectedAtLeastOne = isSelectedAtLeastOne,
                isPaginating = screenState.isPaginating,
                isReplying = screenState.replyTitle != null,
                messageBarHeight = messageBarHeight,
                onRequestScrollToCmId = { cmId ->
                    val index = uiMessages.values.indexOfMessageByCmId(cmId)
                    if (index == null) { // сообщения нет в списке
                        // pizdets
                    } else {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = index)
                        }
                    }
                },
                onMessageClicked = { id ->
                    if (selectedMessages.isNotEmpty()) {
                        if (AppSettings.General.enableHaptic) {
                            view.performHapticFeedback(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                        }
                    }
                    currentOnMessageClicked.invoke(id)
                },
                onMessageLongClicked = onMessageLongClicked,
                onPhotoClicked = onPhotoClicked,
                onRequestMessageReply = onRequestReplyToMessage
            )

            InputBar(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 8.dp),
                message = screenState.message,
                onMessageInputChanged = onMessageInputChanged,
                onBoldRequested = onBoldRequested,
                onItalicRequested = onItalicRequested,
                onUnderlineRequested = onUnderlineRequested,
                onLinkRequested = onLinkRequested,
                onRegularRequested = onRegularRequested,
                hazeState = hazeState,
                showEmojiButton = showEmojiButton,
                showAttachmentButton = showAttachmentButton,
                actionMode = screenState.actionMode,
                replyTitle = screenState.replyTitle,
                replyText = screenState.replyText,
                inputFieldFocusRequester = inputFieldFocusRequester,
                onSetMessageBarHeight = { messageBarHeight = it },
                onEmojiButtonLongClicked = onEmojiButtonLongClicked,
                onAttachmentButtonClicked = onAttachmentButtonClicked,
                onActionButtonClicked = onActionButtonClicked,
                onReplyCloseClicked = onReplyCloseClicked
            )

            when {
                screenState.isLoading && messages.values.isEmpty() -> {
                    Loader(modifier = Modifier.align(Alignment.Center))
                }

                baseError != null -> {
                    VkErrorView(baseError = baseError)
                }
            }
        }
    }
}
