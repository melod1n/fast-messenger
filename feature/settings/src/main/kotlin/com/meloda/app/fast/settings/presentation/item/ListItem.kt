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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.meloda.app.fast.settings.model.UiItem
import com.meloda.app.fast.ui.R
import com.meloda.app.fast.ui.basic.ContentAlpha
import com.meloda.app.fast.ui.basic.LocalContentAlpha
import com.meloda.app.fast.ui.components.ActionInvokeDismiss
import com.meloda.app.fast.ui.components.MaterialDialog
import com.meloda.app.fast.ui.components.SelectionType
import com.meloda.app.fast.ui.theme.LocalTheme
import com.meloda.app.fast.ui.util.ImmutableList
import com.meloda.app.fast.ui.util.ImmutableList.Companion.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItem(
    item: UiItem.List<*>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onChanged: (newValue: Any?) -> Unit
) {
    if (!item.isVisible) return

    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val currentTheme = LocalTheme.current

    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .animateContentSize()
            .combinedClickable(
                enabled = item.isEnabled,
                onClick = {
                    onClick()
                    showDialog = true
                },
                onLongClick = onLongClick,
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
                alpha = if (item.isEnabled) ContentAlpha.high else ContentAlpha.disabled
            ) {
                item.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = if (currentTheme.isMultiline) Int.MAX_VALUE else 1,
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
                        maxLines = if (currentTheme.isMultiline) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
    }

    if (showDialog) {
        ListAlertDialog(
            item = item,
            onDismissAction = { showDialog = false },
            onConfirmButtonClicked = onChanged
        )
    }
}


@Composable
fun ListAlertDialog(
    item: UiItem.List<*>,
    onDismissAction: () -> Unit,
    onConfirmButtonClicked: (newValue: Any?) -> Unit
) {
    val currentValueIndex = remember {
        item.values.indexOf(item.selectedValue)
    }
    var selectedOptionIndex by rememberSaveable {
        mutableIntStateOf(currentValueIndex)
    }

    MaterialDialog(
        onDismissRequest = onDismissAction,
        title = item.title,
        items = item.titles.toImmutableList(),
        preSelectedItems = ImmutableList.of(selectedOptionIndex),
        selectionType = SelectionType.Single,
        onItemClick = { newIndex -> selectedOptionIndex = newIndex },
        confirmText = stringResource(id = R.string.ok),
        confirmAction = {
            if (currentValueIndex != selectedOptionIndex) {
                onConfirmButtonClicked(item.values[selectedOptionIndex])
            }
        },
        actionInvokeDismiss = ActionInvokeDismiss.Always
    )
}
