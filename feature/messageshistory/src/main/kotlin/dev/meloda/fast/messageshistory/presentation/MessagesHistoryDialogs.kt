package dev.meloda.fast.messageshistory.presentation

import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.messageshistory.model.MessageDialog
import dev.meloda.fast.messageshistory.model.MessageOption
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.basic.ContentAlpha
import dev.meloda.fast.ui.basic.LocalContentAlpha
import dev.meloda.fast.ui.components.MaterialDialog
import java.util.concurrent.TimeUnit

@Composable
fun HandleDialogs(
    screenState: MessagesHistoryScreenState,
    dialog: MessageDialog?,
    onConfirmed: (MessageDialog, Bundle) -> Unit = { _, _ -> },
    onDismissed: (MessageDialog) -> Unit = {},
    onItemPicked: (MessageDialog, Bundle) -> Unit = { _, _ -> }
) {
    when (dialog) {
        null -> Unit

        is MessageDialog.MessageOptions -> {
            MessageOptionsDialog(
                screenState = screenState,
                message = dialog.message,
                onDismissed = { onDismissed(dialog) },
                onItemPicked = { bundle -> onItemPicked(dialog, bundle) }
            )
        }

        is MessageDialog.MessageDelete -> {
            MessageDeleteDialog(
                messages = listOf(dialog.message),
                onConfirmed = { onConfirmed(dialog, it) },
                onDismissed = { onDismissed(dialog) }
            )
        }

        is MessageDialog.MessagesDelete -> {
            MessageDeleteDialog(
                messages = dialog.messages,
                onConfirmed = { onConfirmed(dialog, it) },
                onDismissed = { onDismissed(dialog) }
            )
        }

        is MessageDialog.MessagePin,
        is MessageDialog.MessageUnpin -> {
            MessagePinStateDialog(
                pin = dialog is MessageDialog.MessagePin,
                onConfirmed = { onConfirmed(dialog, bundleOf()) },
                onDismissed = { onDismissed(dialog) }
            )
        }

        is MessageDialog.MessageMarkImportance -> {
            MessageImportanceDialog(
                important = dialog.isImportant,
                onConfirmed = { onConfirmed(dialog, bundleOf()) },
                onDismissed = { onDismissed(dialog) }
            )
        }

        is MessageDialog.MessageSpam -> {
            MessageSpamDialog(
                spam = dialog.isSpam,
                onConfirmed = { onConfirmed(dialog, bundleOf()) },
                onDismissed = { onDismissed(dialog) }
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

        if (!message.isOut && !message.isRead(screenState.conversation)) {
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
        title = stringResource(R.string.delete_message_title),
        confirmText = stringResource(R.string.action_delete),
        confirmAction = {
            onConfirmed(
                bundleOf("everyone" to if (messages.all(VkMessage::isOut)) forEveryone else false)
            )
        },
        cancelText = stringResource(R.string.cancel),
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
                Text(text = stringResource(R.string.delete_message_for_everyone))
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
            if (pin) R.string.pin_message_title
            else R.string.unpin_message_title
        ),
        text = stringResource(
            if (pin) R.string.pin_message_text
            else R.string.unpin_message_text
        ),
        confirmText = stringResource(
            if (pin) R.string.action_pin
            else R.string.action_unpin
        ),
        confirmAction = onConfirmed,
        cancelText = stringResource(R.string.cancel)
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
            if (important) R.string.important_message_title
            else R.string.unimportant_message_title
        ),
        text = stringResource(
            if (important) R.string.important_message_text
            else R.string.unimportant_message_text
        ),
        confirmText = stringResource(
            if (important) R.string.action_mark
            else R.string.action_unmark
        ),
        confirmAction = onConfirmed,
        cancelText = stringResource(R.string.cancel)
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
            if (spam) R.string.spam_message_title
            else R.string.unspam_message_title
        ),
        text = stringResource(
            if (spam) R.string.spam_message_text
            else R.string.unspam_message_text
        ),
        confirmText = stringResource(
            if (spam) R.string.action_mark
            else R.string.action_unmark
        ),
        confirmAction = onConfirmed,
        cancelText = stringResource(R.string.cancel)
    )
}
