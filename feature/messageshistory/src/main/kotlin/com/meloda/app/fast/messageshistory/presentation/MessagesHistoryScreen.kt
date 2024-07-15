package com.meloda.app.fast.messageshistory.presentation

import android.content.SharedPreferences
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModel
import com.meloda.app.fast.messageshistory.MessagesHistoryViewModelImpl
import com.meloda.app.fast.messageshistory.model.ActionMode
import com.meloda.app.fast.messageshistory.model.MessagesHistoryScreenState
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.ui.theme.LocalThemeConfig
import com.meloda.app.fast.ui.util.ImmutableList
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import com.meloda.app.fast.ui.R as UiR

@Composable
fun MessagesHistoryRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onChatMaterialsDropdownItemClicked: (peerId: Int, conversationMessageId: Int) -> Unit,
    viewModel: MessagesHistoryViewModel = koinViewModel<MessagesHistoryViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val baseError by viewModel.baseError.collectAsStateWithLifecycle()
    val canPaginate by viewModel.canPaginate.collectAsStateWithLifecycle()

    MessagesHistoryScreen(
        screenState = screenState,
        baseError = baseError,
        canPaginate = canPaginate,
        onBack = onBack,
        onChatMaterialsDropdownItemClicked = onChatMaterialsDropdownItemClicked,
        onRefreshDropdownItemClicked = viewModel::onRefresh,
        onToggleAnimationsDropdownItemClicked = viewModel::onToggleAnimationsDropdownItemClicked,
        onPaginationConditionsMet = viewModel::onPaginationConditionsMet,
        onMessageInputChanged = viewModel::onMessageInputChanged,
        onAttachmentButtonClicked = viewModel::onAttachmentButtonClicked,
        onActionButtonClicked = viewModel::onActionButtonClicked
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
    baseError: BaseError? = null,
    canPaginate: Boolean = false,
    onBack: () -> Unit = {},
    onChatMaterialsDropdownItemClicked: (peerId: Int, conversationMessageId: Int) -> Unit = { _, _ -> },
    onRefreshDropdownItemClicked: () -> Unit = {},
    onToggleAnimationsDropdownItemClicked: (Boolean) -> Unit = {},
    onPaginationConditionsMet: () -> Unit = {},
    onMessageInputChanged: (String) -> Unit = {},
    onAttachmentButtonClicked: () -> Unit = {},
    onActionButtonClicked: () -> Unit = {}
) {
    val view = LocalView.current

    val preferences: SharedPreferences = koinInject()
    val currentTheme = LocalThemeConfig.current

    val listState = rememberLazyListState()

    val paginationConditionMet by remember {
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

    val hazeSate = remember { HazeState() }

    var animationsEnabled by remember {
        mutableStateOf(
            preferences.getBoolean(
                SettingsKeys.KEY_ENABLE_ANIMATIONS_IN_MESSAGES,
                false
            )
        )
    }

    val toolbarColorAlpha by animateFloatAsState(
        targetValue = if (!listState.canScrollForward) 1f else 0f,
        label = "toolbarColorAlpha",
        animationSpec = tween(durationMillis = 50)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    modifier = Modifier
                        .then(
                            if (currentTheme.enableBlur) {
                                Modifier.hazeChild(
                                    state = hazeSate,
                                    style = HazeMaterials.thick()
                                )
                            } else Modifier
                        )
                        .fillMaxWidth(),
                    title = {
                        Text(
                            text =
                            if (screenState.isLoading) stringResource(id = UiR.string.title_loading)
                            else screenState.title
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back button"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(
                            alpha = if (currentTheme.enableBlur) toolbarColorAlpha else 1f
                        )
                    ),
                    actions = {
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

                                    onChatMaterialsDropdownItemClicked(
                                        screenState.conversationId,
                                        screenState.messages.first().conversationMessageId
                                    )
                                },
                                text = {
                                    Text(text = "Materials")
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    onRefreshDropdownItemClicked()
                                    dropDownMenuExpanded = false
                                },
                                text = {
                                    Text(text = "Refresh")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = null
                                    )
                                }
                            )

                            if (preferences.getBoolean(
                                    SettingsKeys.KEY_SHOW_DEBUG_CATEGORY,
                                    false
                                )
                            ) {
                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = {
                                        Text(text = if (animationsEnabled) "Disable animations" else "Enable animations")
                                    },
                                    onClick = {
                                        dropDownMenuExpanded = false
                                        animationsEnabled = !animationsEnabled
                                        onToggleAnimationsDropdownItemClicked(animationsEnabled)
                                    }
                                )
                            }
                        }
                    }
                )
                if (screenState.isLoading && screenState.messages.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
                hazeState = hazeSate,
                listState = listState,
                immutableMessages = ImmutableList.copyOf(screenState.messages),
                isPaginating = screenState.isPaginating,
                enableAnimations = animationsEnabled
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Color.Transparent)
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
                            .weight(1f)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(6.dp))

                        if (
                            preferences.getBoolean(
                                SettingsKeys.KEY_SHOW_EMOJI_BUTTON,
                                SettingsKeys.DEFAULT_VALUE_KEY_SHOW_EMOJI_BUTTON
                            )
                        ) {
                            val scope = rememberCoroutineScope()
                            val rotation = remember { Animatable(0f) }

                            IconButton(
                                onClick = {
                                    view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)

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
                                modifier = Modifier.rotate(rotation.value)
                            ) {
                                Icon(
                                    painter = painterResource(id = UiR.drawable.ic_outline_emoji_emotions_24),
                                    contentDescription = "Emoji button",
                                    tint = MaterialTheme.colorScheme.primary
                                )
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

                        IconButton(onClick = onAttachmentButtonClicked) {
                            Icon(
                                painter = painterResource(id = UiR.drawable.round_attach_file_24),
                                contentDescription = "Add attachment button",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.rotate(30f)
                            )
                        }

                        val scope = rememberCoroutineScope()
                        val rotation = remember { Animatable(0f) }

                        IconButton(
                            onClick = {
                                if (screenState.actionMode == ActionMode.Record) {
                                    view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)

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
                                } else {
                                    onActionButtonClicked()
                                }
                            },
                            modifier = Modifier.rotate(rotation.value)
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

                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }

            if (screenState.isLoading && screenState.messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
