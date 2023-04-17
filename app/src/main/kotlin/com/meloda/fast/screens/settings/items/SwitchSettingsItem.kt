package com.meloda.fast.screens.settings.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.ext.isTrue
import com.meloda.fast.model.base.asString
import com.meloda.fast.screens.settings.model.OnSettingsChangeListener
import com.meloda.fast.screens.settings.model.OnSettingsClickListener
import com.meloda.fast.screens.settings.model.OnSettingsLongClickListener
import com.meloda.fast.screens.settings.model.SettingsItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwitchSettingsItem(
    item: SettingsItem.Switch,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener,
    onSettingsChangeListener: OnSettingsChangeListener
) {
    var isChecked by remember {
        mutableStateOf(item.value.isTrue)
    }

    val onCheckedChange = { newValue: Boolean ->
        isChecked = newValue

        if (item.value != isChecked) {
            item.value = isChecked
            onSettingsChangeListener.onChange(item.key, isChecked)
        }
    }

    var title by remember { mutableStateOf(item.title) }
    item.onTitleChanged = { newTitle -> title = newTitle }

    var summary by remember { mutableStateOf(item.summary) }
    item.onSummaryChanged = { newSummary -> summary = newSummary }

    var value by remember { mutableStateOf(item.value) }
    item.onValueChanged = { newValue ->
        value = newValue
        isChecked = newValue.isTrue
    }

    // TODO: 07.04.2023, Danil Nikolaev: handle isEnabled
    var isEnabled by remember { mutableStateOf(item.isEnabled) }
    item.onEnabledStateChanged = { newEnabled -> isEnabled = newEnabled }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

    if (!isVisible) return
    Row(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 56.dp)
            .combinedClickable(
                enabled = isEnabled,
                onClick = {
                    onSettingsClickListener.onClick(item.key)
                    onCheckedChange.invoke(!isChecked)
                },
                onLongClick = { onSettingsLongClickListener.onLongClick(item.key) },
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
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
        Row {
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                enabled = isEnabled,
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}
