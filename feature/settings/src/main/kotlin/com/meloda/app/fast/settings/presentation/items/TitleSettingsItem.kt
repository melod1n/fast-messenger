package com.meloda.app.fast.settings.presentation.items

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.settings.model.SettingsItem
import com.meloda.app.fast.ui.util.getString

@Composable
fun TitleSettingsItem(
    item: SettingsItem.Title,
    isMultiline: Boolean,
    modifier: Modifier
) {
    var title by remember { mutableStateOf(item.title) }
    item.onTitleChanged = { newTitle -> title = newTitle }

    var isVisible by remember { mutableStateOf(item.isVisible) }
    item.onVisibleStateChanged = { newVisible -> isVisible = newVisible }

    if (!isVisible) return

    Text(
        text = title.getString().orEmpty(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(
                top = 14.dp,
                end = 16.dp,
                start = 16.dp,
                bottom = 4.dp
            )
            .animateContentSize(),
        maxLines = if (isMultiline) Int.MAX_VALUE else 1,
        overflow = TextOverflow.Ellipsis,
    )
}
