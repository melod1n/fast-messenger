package dev.meloda.fast.auth.login.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.meloda.fast.auth.BuildConfig
import dev.meloda.fast.ui.components.ActionInvokeDismiss
import dev.meloda.fast.ui.components.MaterialDialog

import dev.meloda.fast.ui.R as UiR

@Composable
fun SignInAlert(
    onDismissRequest: () -> Unit = {},
    onConfirmClick: (token: String) -> Unit = {}
) {
    var tokenText by rememberSaveable {
        mutableStateOf(BuildConfig.debugToken)
    }

    val maxWidthModifier = Modifier.fillMaxWidth()

    MaterialDialog(
        onDismissRequest = onDismissRequest,
        title = "Fast authorization",
        confirmText = stringResource(id = UiR.string.action_authorize),
        confirmAction = { onConfirmClick(tokenText) },
        cancelText = stringResource(id = UiR.string.cancel),
        actionInvokeDismiss = ActionInvokeDismiss.Always
    ) {
        Column(modifier = maxWidthModifier) {
            OutlinedTextField(
                modifier = maxWidthModifier.padding(horizontal = 16.dp),
                value = tokenText,
                onValueChange = { tokenText = it },
                placeholder = { Text(text = "Access token") },
                label = { Text(text = "Access token") }
            )
        }
    }
}
