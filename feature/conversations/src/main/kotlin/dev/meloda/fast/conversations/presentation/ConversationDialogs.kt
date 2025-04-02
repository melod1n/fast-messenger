package dev.meloda.fast.conversations.presentation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import dev.meloda.fast.conversations.model.ConversationDialog
import dev.meloda.fast.conversations.model.ConversationsScreenState
import dev.meloda.fast.ui.components.MaterialDialog

import dev.meloda.fast.ui.R as UiR

@Composable
fun HandleDialogs(
    screenState: ConversationsScreenState,
    dialog: ConversationDialog?,
    onConfirmed: (ConversationDialog, Bundle) -> Unit = { _, _ -> },
    onDismissed: (ConversationDialog) -> Unit = {},
    onItemPicked: (ConversationDialog, Bundle) -> Unit = { _, _ -> }
) {
    when (dialog) {
        null -> Unit

        is ConversationDialog.ConversationArchive -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = UiR.string.confirm_archive_conversation),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = UiR.string.action_archive),
                cancelText = stringResource(id = UiR.string.cancel)
            )
        }

        is ConversationDialog.ConversationUnarchive -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = UiR.string.confirm_unarchive_conversation),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = UiR.string.action_unarchive),
                cancelText = stringResource(id = UiR.string.cancel)
            )
        }

        is ConversationDialog.ConversationDelete -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = UiR.string.confirm_delete_conversation),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = UiR.string.action_delete),
                cancelText = stringResource(id = UiR.string.cancel)
            )
        }

        is ConversationDialog.ConversationPin -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = UiR.string.confirm_pin_conversation),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = UiR.string.action_pin),
                cancelText = stringResource(id = UiR.string.cancel)
            )
        }

        is ConversationDialog.ConversationUnpin -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = UiR.string.confirm_unpin_conversation),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = UiR.string.action_unpin),
                cancelText = stringResource(id = UiR.string.cancel)
            )
        }
    }
}
