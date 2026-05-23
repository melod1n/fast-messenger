package dev.meloda.fast.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.components.MaterialDialog

@Composable
fun AppCrashedDialog(
    stacktrace: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTrace by rememberSaveable { mutableStateOf(false) }

    MaterialDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.title_error),
        text = if (showTrace) stacktrace else stringResource(R.string.error_occurred),
        confirmText = stringResource(R.string.action_share),
        confirmAction = onShare,
        cancelText = stringResource(if (showTrace) R.string.action_hide_stacktrace else R.string.action_show_stacktrace),
        cancelAction = { showTrace = !showTrace },
        neutralText = stringResource(R.string.action_close),
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
