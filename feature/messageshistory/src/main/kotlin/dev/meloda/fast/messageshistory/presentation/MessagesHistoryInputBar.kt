package dev.meloda.fast.messageshistory.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.contextmenu.builder.item
import androidx.compose.foundation.text.contextmenu.modifier.appendTextContextMenuComponents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.ui.components.IconButton
import dev.meloda.fast.ui.theme.LocalThemeConfig
import dev.meloda.fast.ui.R as UiR

@OptIn(ExperimentalLayoutApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun MessagesHistoryInputBar(
    modifier: Modifier = Modifier,
    message: TextFieldValue,
    hazeState: HazeState,
    enableHaptic: Boolean,
    showEmojiButton: Boolean,
    showAttachmentButton: Boolean,
    actionMode: ActionMode,
    onMessageInputChanged: (TextFieldValue) -> Unit = {},
    onBoldRequested: () -> Unit = {},
    onItalicRequested: () -> Unit = {},
    onUnderlineRequested: () -> Unit = {},
    onLinkRequested: () -> Unit = {},
    onRegularRequested: () -> Unit = {},
    onSetMessageBarHeight: (Dp) -> Unit = {},
    onEmojiButtonLongClicked: () -> Unit = {},
    onAttachmentButtonClicked: () -> Unit = {},
    onActionButtonClicked: () -> Unit = {}
) {
    val view = LocalView.current
    val context = LocalContext.current
    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val theme = LocalThemeConfig.current

    Column(
        modifier = modifier
            .fillMaxWidth()
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
                        with(density) {
                            onSetMessageBarHeight(it.size.height.toDp())
                        }
                    },
                verticalAlignment = Alignment.Bottom
            ) {
                Spacer(modifier = Modifier.width(6.dp))

                if (showEmojiButton) {
                    Column(verticalArrangement = Arrangement.Bottom) {
                        IconButton(
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
                                painter = painterResource(id = UiR.drawable.ic_outline_emoji_emotions_24),
                                contentDescription = "Emoji button",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .appendTextContextMenuComponents {
                            separator()

                            item(
                                key = "Bold",
                                label = context.getString(UiR.string.bold)
                            ) {
                                onBoldRequested()
                                close()
                            }
                            item(
                                key = "Italic",
                                label = context.getString(UiR.string.italic)
                            ) {
                                onItalicRequested()
                                close()
                            }
                            item(
                                key = "Underline",
                                label = context.getString(UiR.string.underline)
                            ) {
                                onUnderlineRequested()
                                close()
                            }
                            item(
                                key = "Link",
                                label = context.getString(UiR.string.link)
                            ) {
                                onLinkRequested()
                                close()
                            }
                            item(
                                key = "Regular",
                                label = context.getString(UiR.string.regular)
                            ) {
                                onRegularRequested()
                                close()
                            }

                            separator()
                        },
                    value = message,
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
                    },
                )

                if (showAttachmentButton) {
                    Column(verticalArrangement = Arrangement.Bottom) {
                        IconButton(
                            onClick = {
                                onAttachmentButtonClicked()
                                if (enableHaptic) {
                                    view.performHapticFeedback(
                                        HapticFeedbackConstantsCompat.REJECT
                                    )
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = UiR.drawable.round_attach_file_24),
                                contentDescription = "Add attachment button",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Column(verticalArrangement = Arrangement.Bottom) {
                    IconButton(
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
                                        ActionMode.DELETE -> UiR.drawable.round_delete_outline_24
                                        ActionMode.EDIT -> UiR.drawable.ic_round_done_24
                                        ActionMode.RECORD_AUDIO -> UiR.drawable.ic_round_mic_none_24
                                        ActionMode.RECORD_VIDEO -> UiR.drawable.rounded_photo_camera_24
                                        ActionMode.SEND -> UiR.drawable.round_send_24
                                    }
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}
