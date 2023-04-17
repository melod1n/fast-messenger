package com.meloda.fast.screens.settings.items

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.fast.model.base.asString
import com.meloda.fast.screens.settings.model.SettingsItem

@Composable
fun TitleSettingsItem(
    item: SettingsItem.Title,
    isMultiline: Boolean
) {
    var title by remember { mutableStateOf(item.title) }
    item.onTitleChanged = { newTitle -> title = newTitle }

    // TODO: 07.04.2023, Danil Nikolaev: handle isEnabled
    var isEnabled by remember { mutableStateOf(item.isEnabled) }
    item.onEnabledStateChanged = { newEnabled -> isEnabled = newEnabled }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

    if (!isVisible) return

    Text(
        text = title.asString().orEmpty(),
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
