package com.meloda.app.fast.settings.presentation.items

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.designsystem.ContentAlpha
import com.meloda.app.fast.designsystem.LocalContentAlpha
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.designsystem.getString
import com.meloda.app.fast.settings.model.OnSettingsChangeListener
import com.meloda.app.fast.settings.model.OnSettingsClickListener
import com.meloda.app.fast.settings.model.OnSettingsLongClickListener
import com.meloda.app.fast.settings.model.SettingsItem
import com.meloda.app.fast.designsystem.R as UiR

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTextSettingsItem(
    item: SettingsItem.TextField,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener,
    onSettingsChangeListener: OnSettingsChangeListener,
    modifier: Modifier
) {
    var title by remember { mutableStateOf(item.title) }
    item.onTitleChanged = { newTitle -> title = newTitle }

    var summary by remember { mutableStateOf(item.summary) }
    item.onSummaryChanged = { newSummary -> summary = newSummary }

    var isEnabled by remember { mutableStateOf(item.isEnabled) }
    item.onEnabledStateChanged = { newEnabled -> isEnabled = newEnabled }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

    var showDialog by remember {
        mutableStateOf(false)
    }

    if (showDialog) {
        EditTextAlert(
            item = item,
            onSettingsChangeListener = { key, newValue ->
                summary = item.summaryProvider?.provideSummary(item)
                onSettingsChangeListener.onChange(key, newValue)
            },
            onDismiss = { showDialog = false }
        )
    }

    if (!isVisible) return
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                enabled = isEnabled,
                onClick = {
                    onSettingsClickListener.onClick(item.key)
                    showDialog = true
                },
                onLongClick = { onSettingsLongClickListener.onLongClick(item.key) },
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
                alpha = if (isEnabled) ContentAlpha.high else ContentAlpha.disabled
            ) {
                title?.getString()?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            LocalContentAlpha(
                alpha = if (isEnabled) ContentAlpha.medium else ContentAlpha.disabled
            ) {
                summary?.getString()?.let { summary ->
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditTextAlert(
    item: SettingsItem.TextField,
    onSettingsChangeListener: OnSettingsChangeListener,
    onDismiss: () -> Unit
) {
    val (textFieldFocusable) = FocusRequester.createRefs()

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(item.value.orEmpty()))
    }

    MaterialDialog(
        title = item.title,
        confirmText = UiText.Resource(UiR.string.ok),
        confirmAction = {
            val newValue = textFieldValue.text.trim()

            if (item.value != newValue) {
                item.value = newValue
                onSettingsChangeListener.onChange(item.key, newValue)
            }
        },
        cancelText = UiText.Resource(UiR.string.cancel),
        onDismissAction = onDismiss
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
