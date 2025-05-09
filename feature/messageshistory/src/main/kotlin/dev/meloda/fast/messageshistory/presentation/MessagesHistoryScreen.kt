package dev.meloda.fast.messageshistory.presentation

import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.messageshistory.util.indexOfMessageByCmId
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.components.IconButton
import dev.meloda.fast.ui.components.VkErrorView
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList
import dev.meloda.fast.ui.util.getImage
import kotlinx.coroutines.launch
import dev.meloda.fast.ui.R as UiR

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
    onTopBarClicked: () -> Unit = {},
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
    onDeleteSelectedButtonClicked: () -> Unit = {},
    onBoldRequested: () -> Unit = {},
    onItalicRequested: () -> Unit = {},
    onUnderlineRequested: () -> Unit = {},
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
                        .fillMaxWidth()
                        .then(
                            if (screenState.isLoading && messages.isEmpty()) Modifier
                            else Modifier.clickable {
                                onTopBarClicked()
                            }
                        ),
                    title = {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedMessages.isEmpty()) {
                                val avatar = screenState.avatar.getImage()
                                if (screenState.conversationId == UserConfig.userId) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp),
                                            painter = painterResource(id = UiR.drawable.ic_round_bookmark_border_24),
                                            contentDescription = "Favorites icon",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                } else {
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
                                }

                                Spacer(modifier = Modifier.width(12.dp))
                            }

                            Text(
                                text = when {
                                    screenState.isLoading -> stringResource(id = UiR.string.title_loading)
                                    selectedMessages.isNotEmpty() -> "(${selectedMessages.size})"
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
                            Crossfade(targetState = selectedMessages.isEmpty()) { state ->
                                Icon(
                                    imageVector = if (state) {
                                        Icons.AutoMirrored.Rounded.ArrowBack
                                    } else {
                                        Icons.Rounded.Close
                                    },
                                    contentDescription = if (state) "Close button"
                                    else "Back button"
                                )
                            }
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
                                    Modifier
                                        .hazeEffect(
                                            state = hazeState,
                                            style = HazeMaterials.ultraThin()
                                        )
                                        .border(
                                            1.dp, MaterialTheme.colorScheme.outlineVariant,
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

                        val view = LocalView.current
                        val textToolbar = remember {
                            CustomTextToolbar(
                                view = view,
                                onBoldRequested = onBoldRequested,
                                onItalicRequested = onItalicRequested,
                                onUnderlineRequested = onUnderlineRequested,
                                onLinkRequested = {}
                            )
                        }

                        CompositionLocalProvider(LocalTextToolbar provides textToolbar) {
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
                        }

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

class CustomTextToolbar(
    private val view: View,
    private var onBoldRequested: (() -> Unit)? = null,
    private var onItalicRequested: (() -> Unit)? = null,
    private var onUnderlineRequested: (() -> Unit)? = null,
    private var onLinkRequested: (() -> Unit)? = null
) : TextToolbar {
    private var actionMode: android.view.ActionMode? = null
    private val textActionModeCallback: TextActionModeCallback =
        TextActionModeCallback(onActionModeDestroy = { actionMode = null })
    override var status: TextToolbarStatus = TextToolbarStatus.Hidden
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
        onAutofillRequested: (() -> Unit)?
    ) {
        textActionModeCallback.rect = rect
        textActionModeCallback.onCopyRequested = onCopyRequested
        textActionModeCallback.onCutRequested = onCutRequested
        textActionModeCallback.onPasteRequested = onPasteRequested
        textActionModeCallback.onSelectAllRequested = onSelectAllRequested
        textActionModeCallback.onAutofillRequested = onAutofillRequested
        textActionModeCallback.onBoldRequested = onBoldRequested
        textActionModeCallback.onItalicRequested = onItalicRequested
        textActionModeCallback.onUnderlineRequested = onUnderlineRequested
        textActionModeCallback.onLinkRequested = onLinkRequested

        if (actionMode == null) {
            status = TextToolbarStatus.Shown
            actionMode =
                TextToolbarHelperMethods.startActionMode(
                    view,
                    FloatingTextActionModeCallback(textActionModeCallback),
                    android.view.ActionMode.TYPE_FLOATING
                )
        } else {
            actionMode?.invalidate()
        }
    }

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        showMenu(
            rect = rect,
            onCopyRequested = onCopyRequested,
            onPasteRequested = onPasteRequested,
            onCutRequested = onCutRequested,
            onSelectAllRequested = onSelectAllRequested,
            onAutofillRequested = null
        )
    }

    override fun hide() {
        status = TextToolbarStatus.Hidden
        actionMode?.finish()
        actionMode = null
    }
}

/**
 * This class is here to ensure that the classes that use this API will get verified and can be AOT
 * compiled. It is expected that this class will soft-fail verification, but the classes which use
 * this method will pass.
 */
internal object TextToolbarHelperMethods {
    fun startActionMode(
        view: View,
        actionModeCallback: android.view.ActionMode.Callback,
        type: Int
    ): android.view.ActionMode? {
        return view.startActionMode(actionModeCallback, type)
    }

    fun invalidateContentRect(actionMode: android.view.ActionMode) {
        actionMode.invalidateContentRect()
    }
}


class FloatingTextActionModeCallback(private val callback: TextActionModeCallback) :
    android.view.ActionMode.Callback2() {
    override fun onActionItemClicked(mode: android.view.ActionMode?, item: MenuItem?): Boolean {
        return callback.onActionItemClicked(mode, item)
    }

    override fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
        return callback.onCreateActionMode(mode, menu)
    }

    override fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
        return callback.onPrepareActionMode(mode, menu)
    }

    override fun onDestroyActionMode(mode: android.view.ActionMode?) {
        callback.onDestroyActionMode()
    }

    override fun onGetContentRect(
        mode: android.view.ActionMode?,
        view: View?,
        outRect: android.graphics.Rect?
    ) {
        val rect = callback.rect
        outRect?.set(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())
    }
}

class TextActionModeCallback(
    val onActionModeDestroy: (() -> Unit)? = null,
    var rect: Rect = Rect.Zero,
    var onCopyRequested: (() -> Unit)? = null,
    var onPasteRequested: (() -> Unit)? = null,
    var onCutRequested: (() -> Unit)? = null,
    var onSelectAllRequested: (() -> Unit)? = null,
    var onAutofillRequested: (() -> Unit)? = null,
    var onBoldRequested: (() -> Unit)? = null,
    var onItalicRequested: (() -> Unit)? = null,
    var onUnderlineRequested: (() -> Unit)? = null,
    var onLinkRequested: (() -> Unit)? = null
) {
    fun onCreateActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
        requireNotNull(menu) { "onCreateActionMode requires a non-null menu" }
        requireNotNull(mode) { "onCreateActionMode requires a non-null mode" }

        onCopyRequested?.let { addMenuItem(menu, MenuItemOption.Copy) }
        onPasteRequested?.let { addMenuItem(menu, MenuItemOption.Paste) }
        onCutRequested?.let { addMenuItem(menu, MenuItemOption.Cut) }
        onSelectAllRequested?.let { addMenuItem(menu, MenuItemOption.SelectAll) }
        if (onAutofillRequested != null && Build.VERSION.SDK_INT >= 26) {
            addMenuItem(menu, MenuItemOption.Autofill)
        }
        onBoldRequested?.let { addMenuItem(menu, MenuItemOption.Bold) }
        onItalicRequested?.let { addMenuItem(menu, MenuItemOption.Italic) }
        onUnderlineRequested?.let { addMenuItem(menu, MenuItemOption.Underline) }
        onLinkRequested?.let { addMenuItem(menu, MenuItemOption.Link) }
        return true
    }

    // this method is called to populate new menu items when the actionMode was invalidated
    fun onPrepareActionMode(mode: android.view.ActionMode?, menu: Menu?): Boolean {
        if (mode == null || menu == null) return false
        updateMenuItems(menu)
        // should return true so that new menu items are populated
        return true
    }

    fun onActionItemClicked(mode: android.view.ActionMode?, item: MenuItem?): Boolean {
        when (item!!.itemId) {
            MenuItemOption.Copy.ordinal -> onCopyRequested?.invoke()
            MenuItemOption.Paste.ordinal -> onPasteRequested?.invoke()
            MenuItemOption.Cut.ordinal -> onCutRequested?.invoke()
            MenuItemOption.SelectAll.ordinal -> onSelectAllRequested?.invoke()
            MenuItemOption.Autofill.ordinal -> onAutofillRequested?.invoke()
            MenuItemOption.Bold.ordinal -> onBoldRequested?.invoke()
            MenuItemOption.Italic.ordinal -> onItalicRequested?.invoke()
            MenuItemOption.Underline.ordinal -> onUnderlineRequested?.invoke()
            MenuItemOption.Link.ordinal -> onLinkRequested?.invoke()
            else -> return false
        }
        mode?.finish()
        return true
    }

    fun onDestroyActionMode() {
        onActionModeDestroy?.invoke()
    }

    @VisibleForTesting
    internal fun updateMenuItems(menu: Menu) {
        addOrRemoveMenuItem(menu, MenuItemOption.Copy, onCopyRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Paste, onPasteRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Cut, onCutRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.SelectAll, onSelectAllRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Autofill, onAutofillRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Bold, onBoldRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Italic, onItalicRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Underline, onUnderlineRequested)
        addOrRemoveMenuItem(menu, MenuItemOption.Link, onLinkRequested)
    }

    private fun addMenuItem(menu: Menu, item: MenuItemOption) {
        menu
            .add(0, item.ordinal, item.order, item.titleResource)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    private fun addOrRemoveMenuItem(menu: Menu, item: MenuItemOption, callback: (() -> Unit)?) {
        when {
            callback != null && menu.findItem(item.ordinal) == null -> addMenuItem(menu, item)
            callback == null && menu.findItem(item.ordinal) != null -> menu.removeItem(item.ordinal)
        }
    }
}

internal enum class MenuItemOption {
    Copy,
    Paste,
    Cut,
    SelectAll,
    Autofill,
    Bold,
    Italic,
    Underline,
    Link;

    val titleResource: Int
        get() =
            when (this) {
                Copy -> android.R.string.copy
                Paste -> android.R.string.paste
                Cut -> android.R.string.cut
                SelectAll -> android.R.string.selectAll
                Autofill ->
                    if (Build.VERSION.SDK_INT <= 26) {
                        UiR.string.autofill
                    } else {
                        android.R.string.autofill
                    }

                Bold -> UiR.string.bold
                Italic -> UiR.string.italic
                Underline -> UiR.string.underline
                Link -> UiR.string.link
            }

    /** This item will be shown before all items that have order greater than this value. */
    val order = ordinal
}
