package com.meloda.fast.screens.settings.items

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.ext.notNull
import com.meloda.fast.model.settings.SettingsItem

@Composable
fun TitleSettingsItem(
    item: SettingsItem.Title,
    isMultiline: Boolean
) {
    Text(
        text = item.title.notNull(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            top = 14.dp,
            end = 16.dp,
            start = 16.dp,
            bottom = 4.dp
        ),
        maxLines = if (isMultiline) Int.MAX_VALUE else 1,
        overflow = TextOverflow.Ellipsis,
    )
}
