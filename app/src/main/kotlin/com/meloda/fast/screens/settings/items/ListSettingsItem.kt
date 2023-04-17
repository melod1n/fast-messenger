package com.meloda.fast.screens.settings.items

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.R
import com.meloda.fast.ext.ItemsChoiceType
import com.meloda.fast.ext.showDialog
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.asString
import com.meloda.fast.screens.settings.model.OnSettingsChangeListener
import com.meloda.fast.screens.settings.model.OnSettingsClickListener
import com.meloda.fast.screens.settings.model.OnSettingsLongClickListener
import com.meloda.fast.screens.settings.model.SettingsItem

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
        modifier = Modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .combinedClickable(
                enabled = isEnabled,
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
            title?.asString()?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            summary?.asString()?.let { summary ->
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
    val checkedItem = item.values.indexOf(selectedOption)

    context.showDialog(
        title = item.title,
        items = item.valueTitles,
        checkedItems = listOf(checkedItem),
        itemsChoiceType = ItemsChoiceType.SingleChoice,
        itemsClickAction = { index, _ ->
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
