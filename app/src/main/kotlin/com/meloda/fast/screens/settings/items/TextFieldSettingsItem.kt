package com.meloda.fast.screens.settings.items

import android.content.Context
import android.view.LayoutInflater
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.R
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.databinding.ItemSettingsEditTextAlertBinding
import com.meloda.fast.ext.getString
import com.meloda.fast.ext.isUsingCompose
import com.meloda.fast.ext.showDialog
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.settings.model.OnSettingsChangeListener
import com.meloda.fast.screens.settings.model.OnSettingsClickListener
import com.meloda.fast.screens.settings.model.OnSettingsLongClickListener
import com.meloda.fast.screens.settings.model.SettingsItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTextSettingsItem(
    item: SettingsItem.TextField,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener,
    onSettingsChangeListener: OnSettingsChangeListener
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(item.title) }
    item.onTitleChanged = { newTitle -> title = newTitle }

    var summary by remember { mutableStateOf(item.summary) }
    item.onSummaryChanged = { newSummary -> summary = newSummary }

    // TODO: 07.04.2023, Danil Nikolaev: handle isEnabled
    var isEnabled by remember { mutableStateOf(item.isEnabled) }
    item.onEnabledStateChanged = { newEnabled -> isEnabled = newEnabled }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

    var showDialog by remember {
        mutableStateOf(false)
    }

    if (showDialog) {
        ShowEditTextAlert(
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
        modifier = Modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .combinedClickable(
                enabled = isEnabled,
                onClick = {
                    onSettingsClickListener.onClick(item.key)

                    if (isUsingCompose()) {
                        showDialog = true
                        return@combinedClickable
                    }

                    showEditTextAlert(
                        context = context,
                        item = item,
                        onSettingsChangeListener = { key, newValue ->
                            summary = item.summaryProvider?.provideSummary(item)
                            onSettingsChangeListener.onChange(key, newValue)
                        }
                    )
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
            title?.getString()?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            summary?.getString()?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}

private fun showEditTextAlert(
    context: Context,
    item: SettingsItem.TextField,
    onSettingsChangeListener: OnSettingsChangeListener
) {
    val binding = ItemSettingsEditTextAlertBinding.inflate(
        LayoutInflater.from(context), null, false
    )

    binding.editText.setText(item.value)

    context.showDialog(
        title = item.title,
        view = binding.root,
        positiveText = UiText.Resource(R.string.ok),
        positiveAction = {
            val newValue = binding.editText.text.toString()

            if (item.value != newValue) {
                item.value = newValue
                onSettingsChangeListener.onChange(item.key, newValue)
            }
        },
        negativeText = UiText.Resource(R.string.cancel)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ShowEditTextAlert(
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
        positiveText = UiText.Resource(R.string.ok),
        positiveAction = {
            val newValue = textFieldValue.text.trim()

            if (item.value != newValue) {
                item.value = newValue
                onSettingsChangeListener.onChange(item.key, newValue)
            }
        },
        negativeText = UiText.Resource(R.string.cancel),
        onDismissAction = onDismiss
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .focusRequester(textFieldFocusable),
            value = textFieldValue,
            onValueChange = { newText ->
                textFieldValue = newText
            },
            label = { Text(text = "Value") },
            placeholder = { Text(text = "Value") },
            shape = RoundedCornerShape(10.dp),
        )
    }

    LaunchedEffect(Unit) {
        textFieldFocusable.requestFocus()
        textFieldValue = textFieldValue.copy(selection = TextRange(textFieldValue.text.length))
    }
}
