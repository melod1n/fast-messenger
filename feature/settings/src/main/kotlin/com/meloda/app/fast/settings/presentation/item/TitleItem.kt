package com.meloda.app.fast.settings.presentation.item

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.settings.model.UiItem
import com.meloda.app.fast.ui.theme.LocalTheme

@Composable
fun TitleItem(
    item: UiItem.Title,
    modifier: Modifier = Modifier
) {
    if (!item.isVisible) return

    val currentTheme = LocalTheme.current

    Text(
        text = item.title,
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
        maxLines = if (currentTheme.multiline) Int.MAX_VALUE else 1,
        overflow = TextOverflow.Ellipsis,
    )
}
