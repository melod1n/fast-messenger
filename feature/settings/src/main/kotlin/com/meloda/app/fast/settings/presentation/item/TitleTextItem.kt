package com.meloda.app.fast.settings.presentation.item

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.settings.model.UiItem
import com.meloda.app.fast.ui.basic.ContentAlpha
import com.meloda.app.fast.ui.basic.LocalContentAlpha
import com.meloda.app.fast.ui.theme.LocalTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleTextItem(
    item: UiItem.TitleText,
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit = {},
    onLongClick: (key: String) -> Unit = {}
) {
    if (!item.isVisible) return

    val currentTheme = LocalTheme.current

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                enabled = item.isEnabled,
                onClick = { onClick(item.key) },
                onLongClick = { onLongClick(item.key) },
            )
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            LocalContentAlpha(
                alpha = if (item.isEnabled) ContentAlpha.high
                else ContentAlpha.disabled
            ) {
                item.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = if (currentTheme.multiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            LocalContentAlpha(
                alpha = if (item.isEnabled) ContentAlpha.medium else ContentAlpha.disabled
            ) {
                item.text?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (currentTheme.multiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}
