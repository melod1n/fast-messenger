package dev.meloda.fast.settings.presentation.item

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.meloda.fast.settings.model.UiItem
import dev.meloda.fast.ui.theme.LocalThemeConfig

@Composable
fun TitleItem(
    item: UiItem.Title,
    modifier: Modifier = Modifier
) {
    if (!item.isVisible) return

    val currentTheme = LocalThemeConfig.current

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
        maxLines = if (currentTheme.enableMultiline) Int.MAX_VALUE else 1,
        overflow = TextOverflow.Ellipsis,
    )
}
