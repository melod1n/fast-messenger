package com.meloda.fast.screens.settings.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.ext.combinedClickableSound
import com.meloda.fast.model.settings.SettingsItem
import com.meloda.fast.screens.settings.OnSettingsClickListener
import com.meloda.fast.screens.settings.OnSettingsLongClickListener

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleSummarySettingsItem(
    item: SettingsItem.TitleSummary,
    isMultiline: Boolean,
    onSettingsClickListener: OnSettingsClickListener,
    onSettingsLongClickListener: OnSettingsLongClickListener
) {
    val enabled = item.isEnabled

    Row(
        modifier = Modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .combinedClickableSound(
                enabled = enabled,
                onClick = { onSettingsClickListener.onClick(item.key) },
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
            item.summary?.let { summary ->
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
