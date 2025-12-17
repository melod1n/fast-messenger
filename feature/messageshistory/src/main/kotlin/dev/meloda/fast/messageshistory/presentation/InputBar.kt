package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.domain.util.annotated
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.FastTextField
import dev.meloda.fast.ui.components.RippledClickContainer
import dev.meloda.fast.ui.theme.LocalThemeConfig

@OptIn(ExperimentalLayoutApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun InputBar(
    modifier: Modifier = Modifier,
    message: TextFieldValue,
    hazeState: HazeState,
    showEmojiButton: Boolean,
    showAttachmentButton: Boolean,
    actionMode: ActionMode,
    replyTitle: String?,
    replyText: AnnotatedString?,
    inputFieldFocusRequester: Boolean,
    onMessageInputChanged: (TextFieldValue) -> Unit = {},
    onBoldRequested: () -> Unit = {},
    onItalicRequested: () -> Unit = {},
    onUnderlineRequested: () -> Unit = {},
    onLinkRequested: () -> Unit = {},
    onRegularRequested: () -> Unit = {},
    onSetMessageBarHeight: (Dp) -> Unit = {},
    onEmojiButtonLongClicked: () -> Unit = {},
    onAttachmentButtonClicked: () -> Unit = {},
    onActionButtonClicked: () -> Unit = {},
    onReplyCloseClicked: () -> Unit = {}
) {
    val view = LocalView.current
    val context = LocalContext.current
    val density = LocalDensity.current

    val theme = LocalThemeConfig.current

    var localMessage by retain(message) {
        mutableStateOf(message)
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(inputFieldFocusRequester) {
        if (inputFieldFocusRequester) {
            focusRequester.requestFocus()
        }
    }

    val inputBarCornerRadius =
        if (replyTitle == null) (32.dp - if (localMessage.text.lines().size > 1) 8.dp else 0.dp) else 24.dp

    val inputBarTopCornerRadius by animateDpAsState(
        targetValue = if (replyTitle == null) inputBarCornerRadius else 0.dp,
        label = "inputBarTopCornerRadius"
    )

    val inputBarShape = RoundedCornerShape(
        topStart = inputBarTopCornerRadius,
        topEnd = inputBarTopCornerRadius,
        bottomStart = inputBarCornerRadius,
        bottomEnd = inputBarCornerRadius
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .imePadding()
    ) {
        AnimatedVisibility(replyTitle != null) {
            ReplyContainer(
                modifier = Modifier.padding(horizontal = 8.dp),
                title = replyTitle.orEmpty(),
                text = replyText,
                onCloseClicked = onReplyCloseClicked,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .imeNestedScroll(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Row(
                modifier = Modifier
                    .clip(inputBarShape)
                    .then(
                        if (theme.enableBlur) {
                            Modifier
                                .hazeEffect(
                                    state = hazeState,
                                    style = HazeMaterials.thin()
                                )
                        } else Modifier
                    )
                    .weight(1f)
                    .background(
                        if (theme.enableBlur) Color.Transparent
                        else MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                    )
                    .onGloballyPositioned {
                        with(density) {
                            onSetMessageBarHeight(it.size.height.toDp())
                        }
                    },
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.width(6.dp))

                if (showEmojiButton) {
                    Column(verticalArrangement = Arrangement.Bottom) {
                        RippledClickContainer(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            onClick = {
                                if (AppSettings.General.enableHaptic) {
                                    view.performHapticFeedback(
                                        HapticFeedbackConstantsCompat.REJECT
                                    )
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
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_outline_emoji_emotions_24),
                                contentDescription = "Emoji button",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                FastTextField(
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .appendTextContextMenuComponents {
                            separator()

                            item(
                                key = "Bold",
                                label = context.getString(R.string.bold)
                            ) {
                                onBoldRequested()
                                close()
                            }
                            item(
                                key = "Italic",
                                label = context.getString(R.string.italic)
                            ) {
                                onItalicRequested()
                                close()
                            }
                            item(
                                key = "Underline",
                                label = context.getString(R.string.underline)
                            ) {
                                onUnderlineRequested()
                                close()
                            }
                            item(
                                key = "Link",
                                label = context.getString(R.string.link)
                            ) {
                                onLinkRequested()
                                close()
                            }
                            item(
                                key = "Regular",
                                label = context.getString(R.string.regular)
                            ) {
                                onRegularRequested()
                                close()
                            }

                            separator()
                        },
                    value = localMessage,
                    onValueChange = { newValue ->
                        localMessage = newValue
                        onMessageInputChanged(newValue)
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.message_input_hint),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                )

                if (showAttachmentButton) {
                    Column(verticalArrangement = Arrangement.Bottom) {
                        RippledClickContainer(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            onClick = {
                                onAttachmentButtonClicked()
                                if (AppSettings.General.enableHaptic) {
                                    view.performHapticFeedback(
                                        HapticFeedbackConstantsCompat.REJECT
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.round_attach_file_24),
                                contentDescription = "Add attachment button",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }

                Column(verticalArrangement = Arrangement.Bottom) {
                    RippledClickContainer(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        onClick = {
                            onActionButtonClicked()
                            if (AppSettings.General.enableHaptic && actionMode.isRecord()) {
                                view.performHapticFeedback(HapticFeedbackConstantsCompat.CONTEXT_CLICK)
                            }
                        }
                    ) {
                        AnimatedContent(
                            targetState = actionMode,
                            transitionSpec = {
                                (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith
                                        (fadeOut() + scaleOut(targetScale = 1.2f))
                            }
                        ) { actionMode ->
                            Icon(
                                painter = painterResource(
                                    id = when (actionMode) {
                                        ActionMode.DELETE -> R.drawable.round_delete_outline_24
                                        ActionMode.EDIT -> R.drawable.ic_round_done_24
                                        ActionMode.RECORD_AUDIO -> R.drawable.ic_round_mic_none_24
                                        ActionMode.RECORD_VIDEO -> R.drawable.rounded_photo_camera_24
                                        ActionMode.SEND -> R.drawable.round_send_24
                                    }
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Preview
@Composable
private fun InputBarPreview() {
    InputBar(
        message = TextFieldValue("Привет!"),
        hazeState = remember { HazeState() },
        showEmojiButton = true,
        showAttachmentButton = true,
        actionMode = ActionMode.SEND,
        replyTitle = "Иннокентий Панфилович",
        replyText = "Ого, ром!".annotated(),
        inputFieldFocusRequester = false
    )
}
