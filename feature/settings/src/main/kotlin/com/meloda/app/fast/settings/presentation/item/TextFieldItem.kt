package com.meloda.app.fast.settings.presentation.item

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.settings.model.UiItem
import com.meloda.app.fast.ui.R
import com.meloda.app.fast.ui.basic.ContentAlpha
import com.meloda.app.fast.ui.basic.LocalContentAlpha
import com.meloda.app.fast.ui.components.ActionInvokeDismiss
import com.meloda.app.fast.ui.components.MaterialDialog
import com.meloda.app.fast.ui.theme.LocalTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextFieldItem(
    item: UiItem.TextField,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onChanged: (fieldText: String) -> Unit
) {
    if (!item.isVisible) return

    val currentTheme = LocalTheme.current

    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDialog) {
        EditTextAlert(
            item = item,
            onAlertConfirmClicked = onChanged,
            onDismiss = { showDialog = false },
        )
    }

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                enabled = item.isEnabled,
                onClick = {
                    onClick()
                    showDialog = true
                },
                onLongClick = onLongClick,
            )
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            LocalContentAlpha(
                alpha = if (item.isEnabled) ContentAlpha.high else ContentAlpha.disabled
            ) {
                item.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = if (currentTheme.multiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            LocalContentAlpha(
                alpha = if (item.isEnabled) ContentAlpha.medium else ContentAlpha.disabled
            ) {
                item.text?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (currentTheme.multiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
fun EditTextAlert(
    item: UiItem.TextField,
    onAlertConfirmClicked: (newValue: String) -> Unit,
    onDismiss: () -> Unit
) {
    val (textFieldFocusable) = FocusRequester.createRefs()

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(item.fieldText))
    }

    MaterialDialog(
        onDismissRequest = onDismiss,
        title = item.title,
        confirmText = stringResource(id = R.string.ok),
        confirmAction = {
            onAlertConfirmClicked(textFieldValue.text.trim())
        },
        cancelText = stringResource(id = R.string.cancel),
        actionInvokeDismiss = ActionInvokeDismiss.Always
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(20.dp))
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .focusRequester(textFieldFocusable)
                    .weight(1f),
                value = textFieldValue,
                onValueChange = { newText ->
                    textFieldValue = newText
                },
                label = { Text(text = "Value") },
                placeholder = { Text(text = "Value") },
                shape = RoundedCornerShape(10.dp),
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
    }

    LaunchedEffect(Unit) {
        textFieldFocusable.requestFocus()
        textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
    }
}
