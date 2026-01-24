package dev.meloda.fast.convos.presentation

import android.os.Bundle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.core.os.bundleOf
import dev.meloda.fast.convos.model.ConvoDialog
import dev.meloda.fast.convos.model.ConvosScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.MaterialDialog

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
                cancelText = stringResource(id = R.string.cancel),
                icon = ImageVector.vectorResource(R.drawable.ic_archive_fill_round_24)
            )
        }

        is ConvoDialog.ConvoUnarchive -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_unarchive_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_unarchive),
                cancelText = stringResource(id = R.string.cancel),
                icon = ImageVector.vectorResource(R.drawable.ic_unarchive_fill_round_24)
            )
        }

        is ConvoDialog.ConvoDelete -> {
            val errorColor = MaterialTheme.colorScheme.error

            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                icon = ImageVector.vectorResource(R.drawable.ic_delete_fill_round_24),
                iconTint = errorColor,
                title = stringResource(id = R.string.confirm_delete_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_delete),
                confirmContainerColor = errorColor,
                cancelText = stringResource(id = R.string.cancel),
            )
        }

        is ConvoDialog.ConvoPin -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_pin_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_pin),
                cancelText = stringResource(id = R.string.cancel),
                icon = ImageVector.vectorResource(R.drawable.ic_keep_fill_round_24)
            )
        }

        is ConvoDialog.ConvoUnpin -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(id = R.string.confirm_unpin_convo),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = R.string.action_unpin),
                cancelText = stringResource(id = R.string.cancel),
                icon = ImageVector.vectorResource(R.drawable.ic_keep_off_fill_round_24)
            )
        }
    }
}
