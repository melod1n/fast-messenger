package com.meloda.fast.screens.settings.presentation.items

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.meloda.fast.R
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.LocalContentAlpha
import com.meloda.fast.ext.getString
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.model.OnSettingsChangeListener
import com.meloda.fast.screens.settings.model.OnSettingsClickListener
import com.meloda.fast.screens.settings.model.OnSettingsLongClickListener
import com.meloda.fast.screens.settings.model.SettingsItem
import com.meloda.fast.ui.ContentAlpha

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
        confirmText = UiText.Resource(R.string.ok),
        confirmAction = {
            val newValue = textFieldValue.text.trim()

            if (item.value != newValue) {
                item.value = newValue
                onSettingsChangeListener.onChange(item.key, newValue)
            }
        },
        cancelText = UiText.Resource(R.string.cancel),
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
