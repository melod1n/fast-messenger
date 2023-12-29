package com.meloda.fast.screens.settings.presentation.items

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.R
import com.meloda.fast.compose.ItemsSelectionType
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
fun ListSettingsItem(
    item: SettingsItem.ListItem,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener,
    onSettingsChangeListener: OnSettingsChangeListener,
    modifier: Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf(item.title) }
    item.onTitleChanged = { newTitle -> title = newTitle }

    var summary by remember { mutableStateOf(item.summary) }
    item.onSummaryChanged = { newSummary -> summary = newSummary }

    var isEnabled by remember { mutableStateOf(item.isEnabled) }
    item.onEnabledStateChanged = { newEnabled -> isEnabled = newEnabled }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

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
//                    showListAlertDialog(
//                        context = context,
//                        item = item,
//                        onSettingsChangeListener = { key, newValue ->
//                            summary = item.summaryProvider?.provideSummary(item)
//                            onSettingsChangeListener.onChange(key, newValue)
//                        }
//                    )
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

    if (showDialog) {
        ListAlertDialog(
            onDismissAction = {
                showDialog = false
            },
            item = item,
            onSettingsChangeListener = { key, newValue ->
                onSettingsChangeListener.onChange(key, newValue)
                item.updateSummary()
            }
        )
    }
}

@Composable
fun ListAlertDialog(
    onDismissAction: () -> Unit,
    item: SettingsItem.ListItem,
    onSettingsChangeListener: OnSettingsChangeListener
) {
    var selectedOption = item.value
    val checkedItem = item.values.indexOf(selectedOption)

    MaterialDialog(
        onDismissAction = onDismissAction,
        title = item.title,
        items = item.valueTitles,
        preSelectedItems = listOf(checkedItem),
        itemsSelectionType = ItemsSelectionType.Single,
        onItemClick = { index ->
            selectedOption = item.values[index]
        },
        positiveText = UiText.Resource(R.string.ok),
        positiveAction = {
            if (item.value != selectedOption) {
                item.value = selectedOption
                onSettingsChangeListener.onChange(item.key, selectedOption)
            }
        }
    )
}
