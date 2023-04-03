package com.meloda.fast.screens.settings.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.ext.combinedClickableSound
import com.meloda.fast.ext.isTrue
import com.meloda.fast.model.settings.SettingsItem
import com.meloda.fast.screens.settings.OnSettingsChangeListener
import com.meloda.fast.screens.settings.OnSettingsClickListener
import com.meloda.fast.screens.settings.OnSettingsLongClickListener

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
    val enabled = item.isEnabled

    val onCheckedChange = { newValue: Boolean ->
        isChecked = newValue

        if (item.value != isChecked) {
            item.value = isChecked
            onSettingsChangeListener.onChange(item.key, isChecked)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 56.dp)
            .combinedClickableSound(
                enabled = enabled,
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
            item.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            item.summary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isMultiline) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Row {
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                enabled = enabled,
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}
