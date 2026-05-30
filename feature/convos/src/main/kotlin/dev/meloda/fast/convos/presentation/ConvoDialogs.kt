package dev.meloda.fast.convos.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import dev.meloda.fast.convos.model.ConvoDialog
import dev.meloda.fast.convos.model.ConvoIntent
import dev.meloda.fast.convos.model.ConvosScreenState
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.MaterialDialog

@Composable
fun HandleDialogs(
    handleIntent: (ConvoIntent) -> Unit,
    screenState: ConvosScreenState,
) {
    when (screenState.dialog) {
        null -> Unit

        is ConvoDialog.Archive -> {
            MaterialDialog(
                onDismissRequest = { handleIntent(ConvoIntent.Dialog.Dismiss) },
                title = stringResource(id = R.string.confirm_archive_convo),
                confirmAction = { handleIntent(ConvoIntent.Dialog.Confirm()) },
                confirmText = stringResource(id = R.string.action_archive),
                cancelText = stringResource(id = R.string.cancel),
                icon = ImageVector.vectorResource(R.drawable.ic_archive_fill_round_24)
            )
        }

        is ConvoDialog.Unarchive -> {
            MaterialDialog(
                onDismissRequest = { handleIntent(ConvoIntent.Dialog.Dismiss) },
                title = stringResource(id = R.string.confirm_unarchive_convo),
                confirmAction = { handleIntent(ConvoIntent.Dialog.Confirm()) },
                confirmText = stringResource(id = R.string.action_unarchive),
                cancelText = stringResource(id = R.string.cancel),
                icon = ImageVector.vectorResource(R.drawable.ic_unarchive_fill_round_24)
            )
        }

        is ConvoDialog.Delete -> {
            val errorColor = MaterialTheme.colorScheme.error

            MaterialDialog(
                onDismissRequest = { handleIntent(ConvoIntent.Dialog.Dismiss) },
                icon = ImageVector.vectorResource(R.drawable.ic_delete_fill_round_24),
                iconTint = errorColor,
                title = stringResource(id = R.string.confirm_delete_convo),
                confirmAction = { handleIntent(ConvoIntent.Dialog.Confirm()) },
                confirmText = stringResource(id = R.string.action_delete),
                confirmContainerColor = errorColor,
                cancelText = stringResource(id = R.string.cancel),
            )
        }

        is ConvoDialog.Pin -> {
            MaterialDialog(
                onDismissRequest = { handleIntent(ConvoIntent.Dialog.Dismiss) },
                icon = ImageVector.vectorResource(R.drawable.ic_keep_fill_round_24),
                title = stringResource(id = R.string.confirm_pin_convo),
                confirmAction = { handleIntent(ConvoIntent.Dialog.Confirm()) },
                confirmText = stringResource(id = R.string.action_pin),
                cancelText = stringResource(id = R.string.cancel),
            )
        }

        is ConvoDialog.Unpin -> {
            MaterialDialog(
                onDismissRequest = { handleIntent(ConvoIntent.Dialog.Dismiss) },
                icon = ImageVector.vectorResource(R.drawable.ic_keep_off_fill_round_24),
                title = stringResource(id = R.string.confirm_unpin_convo),
                confirmAction = { handleIntent(ConvoIntent.Dialog.Confirm()) },
                confirmText = stringResource(id = R.string.action_unpin),
                cancelText = stringResource(id = R.string.cancel),
            )
        }
    }
}
