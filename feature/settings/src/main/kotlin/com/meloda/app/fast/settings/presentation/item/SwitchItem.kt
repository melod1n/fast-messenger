package com.meloda.app.fast.settings.presentation.item

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.settings.model.UiItem
import com.meloda.app.fast.ui.basic.ContentAlpha
import com.meloda.app.fast.ui.basic.LocalContentAlpha
import com.meloda.app.fast.ui.theme.LocalThemeConfig

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwitchItem(
    item: UiItem.Switch,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onChanged: (isChecked: Boolean) -> Unit
) {
    if (!item.isVisible) return

    val currentTheme = LocalThemeConfig.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .heightIn(min = 56.dp)
            .animateContentSize()
            .combinedClickable(
                enabled = item.isEnabled,
                onClick = {
                    onClick()
                    onChanged(!item.isChecked)
                },
                onLongClick = onLongClick,
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
                alpha = if (item.isEnabled) ContentAlpha.high else ContentAlpha.disabled
            ) {
                item.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = if (currentTheme.enableMultiline) Int.MAX_VALUE else 1,
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
                        maxLines = if (currentTheme.enableMultiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Row {
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                enabled = item.isEnabled,
                checked = item.isChecked,
                onCheckedChange = null
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
    }
}
