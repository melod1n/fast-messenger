package dev.meloda.fast.convos.presentation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import dev.meloda.fast.convos.model.ConvoDialog
import dev.meloda.fast.convos.model.ConvosScreenState
import dev.meloda.fast.ui.components.MaterialDialog

import dev.meloda.fast.ui.R

@Composable
fun HandleDialogs(
    screenState: ConvosScreenState,
    dialog: ConvoDialog?,
    onConfirmed: (ConvoDialog, Bundle) -> Unit = { _, _ -> },
    onDismissed: (ConvoDialog) -> Unit = {},
    onItemPicked: (ConvoDialog, Bundle) -> Unit = { _, _ -> }
) {
    when (dialog) {
        null -> Unit

        is ConvoDialog.ConvoArchive -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_archive_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_archive),
                cancelText = stringResource(id = R.string.cancel)
            )
        }

        is ConvoDialog.ConvoUnarchive -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_unarchive_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_unarchive),
                cancelText = stringResource(id = R.string.cancel)
            )
        }

        is ConvoDialog.ConvoDelete -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_delete_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_delete),
                cancelText = stringResource(id = R.string.cancel)
            )
        }

        is ConvoDialog.ConvoPin -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_pin_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_pin),
                cancelText = stringResource(id = R.string.cancel)
            )
        }

        is ConvoDialog.ConvoUnpin -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_unpin_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_unpin),
                cancelText = stringResource(id = R.string.cancel)
            )
        }
    }
}
