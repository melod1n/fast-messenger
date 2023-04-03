package com.meloda.fast.screens.settings.items

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.ext.combinedClickableSound
import com.meloda.fast.ext.findIndex
import com.meloda.fast.model.settings.SettingsItem
import com.meloda.fast.screens.settings.OnSettingsChangeListener
import com.meloda.fast.screens.settings.OnSettingsClickListener
import com.meloda.fast.screens.settings.OnSettingsLongClickListener

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListSettingsItem(
    item: SettingsItem.ListItem,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener,
    onSettingsChangeListener: OnSettingsChangeListener
) {
    val context = LocalContext.current
    val enabled = item.isEnabled

    var summary by remember {
        mutableStateOf(item.summary)
    }

    Row(
        modifier = Modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .combinedClickableSound(
                enabled = enabled,
                onClick = {
                    onSettingsClickListener.onClick(item.key)
                    showListAlertDialog(
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
            item.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            summary?.let { summary ->
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

private fun showListAlertDialog(
    context: Context,
    item: SettingsItem.ListItem,
    onSettingsChangeListener: OnSettingsChangeListener
) {
    var selectedOption = item.value
    val items = item.valueTitles.toTypedArray()
    val checkedItem = item.values.findIndex { it == (selectedOption ?: 0) } ?: 0

    MaterialAlertDialogBuilder(context)
        .setTitle(item.title)
        .setSingleChoiceItems(items, checkedItem) { _, which ->
            selectedOption = item.values[which]
        }
        .setPositiveButton(R.string.ok) { dialog, _ ->
            if (item.value != selectedOption) {
                item.value = selectedOption
                onSettingsChangeListener.onChange(item.key, selectedOption)
            }
            dialog.dismiss()
        }
        .show()
}
