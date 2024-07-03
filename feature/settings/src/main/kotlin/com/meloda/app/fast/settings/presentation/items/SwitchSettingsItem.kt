package com.meloda.app.fast.settings.presentation.items

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.meloda.app.fast.designsystem.ContentAlpha
import com.meloda.app.fast.designsystem.LocalContentAlpha
import com.meloda.app.fast.designsystem.getString
import com.meloda.app.fast.settings.model.OnSettingsChangeListener
import com.meloda.app.fast.settings.model.OnSettingsClickListener
import com.meloda.app.fast.settings.model.OnSettingsLongClickListener
import com.meloda.app.fast.settings.model.SettingsItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwitchSettingsItem(
    item: SettingsItem.Switch,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener,
    onSettingsChangeListener: OnSettingsChangeListener,
    modifier: Modifier
) {
    var isChecked by remember {
        mutableStateOf(item.value == true)
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
        isChecked = newValue == true
    }

    var isEnabled by remember { mutableStateOf(item.isEnabled) }
    item.onEnabledStateChanged = { newEnabled -> isEnabled = newEnabled }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

    if (!isVisible) return
    Row(
        modifier = modifier
            .fillMaxSize()
            .heightIn(min = 56.dp)
            .animateContentSize()
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