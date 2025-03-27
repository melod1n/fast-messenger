package dev.meloda.fast.messageshistory.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.messageshistory.MessagesHistoryViewModel
import dev.meloda.fast.messageshistory.MessagesHistoryViewModelImpl
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.util.firstMessage
import dev.meloda.fast.messageshistory.util.indexOfMessageByCmId
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.ErrorView
import dev.meloda.fast.ui.components.IconButton
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.getImage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import dev.meloda.fast.ui.R as UiR

@Composable
fun MessagesHistoryRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onChatMaterialsDropdownItemClicked: (peerId: Int, conversationMessageId: Int) -> Unit,
    viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val selectedMessages by viewModel.selectedMessages.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    val userSettings: UserSettings = koinInject()
    val showEmojiButton by userSettings.showEmojiButton.collectAsStateWithLifecycle()

    MessagesHistoryScreen(
        screenState = screenState,
        selectedMessages = ImmutableList.copyOf(selectedMessages),
        baseError = baseError,
        canPaginate = canPaginate,
        showEmojiButton = showEmojiButton,
        onBack = onBack,
        onClose = viewModel::onCloseButtonClicked,
        onSessionExpiredLogOutButtonClicked = { onError(BaseError.SessionExpired) },
        onChatMaterialsDropdownItemClicked = onChatMaterialsDropdownItemClicked,
        onRefresh = viewModel::onRefresh,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onMessageInputChanged = viewModel::onMessageInputChanged,
        onAttachmentButtonClicked = viewModel::onAttachmentButtonClicked,
        onActionButtonClicked = viewModel::onActionButtonClicked,
        onEmojiButtonLongClicked = viewModel::onEmojiButtonLongClicked,
        onMessageClicked = viewModel::onMessageClicked,
        onMessageLongClicked = viewModel::onMessageLongClicked
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
    selectedMessages: ImmutableList<Int> = ImmutableList.empty(),
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    showEmojiButton: Boolean = false,
    onBack: () -> Unit = {},
    onClose: () -> Unit = {},
    onSessionExpiredLogOutButtonClicked: () -> Unit = {},
    onChatMaterialsDropdownItemClicked: (peerId: Int, conversationMessageId: Int) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onMessageInputChanged: (TextFieldValue) -> Unit = {},
    onAttachmentButtonClicked: () -> Unit = {},
    onActionButtonClicked: () -> Unit = {},
    onEmojiButtonLongClicked: () -> Unit = {},
    onMessageClicked: (Int) -> Unit = {},
    onMessageLongClicked: (Int) -> Unit = {}
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val currentTheme = LocalThemeConfig.current
    val listState = rememberLazyListState()
    val hazeState = remember { HazeState() }

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
        targetValue = if (!currentTheme.enableBlur || !listState.canScrollBackward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutLinearInEasing
        )
    )

    val topBarContainerColor by animateColorAsState(
        targetValue =
            if (currentTheme.enableBlur || !listState.canScrollBackward) MaterialTheme.colorScheme.surface
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

    val showReplyAction by remember(screenState) {
        mutableStateOf(selectedMessages.size == 1)
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
                        if (currentTheme.enableBlur) {
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
                            if (currentTheme.enableBlur) {
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
                                            screenState.messages.firstMessage().conversationMessageId
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
                    derivedStateOf { screenState.isLoading && screenState.messages.isNotEmpty() }
                }
                if (showHorizontalProgressBar) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                AnimatedVisibility(!showHorizontalProgressBar) {
                    HorizontalDivider()
                }

                if (!screenState.isLoading && pinnedMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {

                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.rotate(45f),
                            painter = painterResource(UiR.drawable.ic_round_push_pin_24),
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = screenState.pinnedTitle.orDots(),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            screenState.pinnedSummary?.let { summary ->
                                LocalContentAlpha(alpha = ContentAlpha.medium) {
                                    Text(text = summary)
                                }
                            }
                        }
                    }
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
                hazeState = hazeState,
                listState = listState,
                hasPinnedMessage = pinnedMessage != null,
                immutableMessages = ImmutableList.copyOf(screenState.messages),
                isPaginating = screenState.isPaginating,
                messageBarHeight = messageBarHeight,
                onRequestScrollToCmId = { cmId ->
                    coroutineScope.launch {
                        listState.animateScrollToItem(
                            index = screenState.messages.indexOfMessageByCmId(cmId)
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
                            .animateContentSize()
                            .weight(1f)
                            .clip(RoundedCornerShape(36.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp))
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
                screenState.isLoading && screenState.messages.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                baseError != null -> {
                    when (baseError) {
                        is BaseError.SessionExpired -> {
                            ErrorView(
                                text = stringResource(UiR.string.session_expired),
                                buttonText = stringResource(UiR.string.action_log_out),
                                onButtonClick = onSessionExpiredLogOutButtonClicked
                            )
                        }

                        is BaseError.SimpleError -> {
                            ErrorView(
                                text = baseError.message,
                                buttonText = stringResource(UiR.string.try_again),
                                onButtonClick = onRefresh
                            )
                        }
                    }
                }
            }
        }
    }
}
