package com.meloda.fast.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.meloda.fast.ext.getString
import com.meloda.fast.model.base.UiText
import com.meloda.fast.ui.AppTheme

@Preview
@Composable
fun MaterialDialogPreview() {
    AppTheme {
        MaterialDialog(
            onDismissAction = {},
            title = UiText.Simple("Title"),
//            message = UiText.Simple("Message"),
            positiveText = UiText.Simple("Positive"),
            positiveAction = {},
            negativeText = UiText.Simple("Negative"),
            negativeAction = {},
            neutralText = UiText.Simple("Neutral"),
            neutralAction = {},
            items = List(5) { index -> UiText.Simple("Item #${index + 1}") },
            itemsSelectionType = ItemsSelectionType.Multi,
            preSelectedItems = listOf(2),
            customContent = null
        )
    }
}

@Composable
fun MaterialDialog(
    onDismissAction: (() -> Unit),
    title: UiText? = null,
    message: UiText? = null,
    positiveText: UiText? = null,
    positiveAction: (() -> Unit)? = null,
    negativeText: UiText? = null,
    negativeAction: (() -> Unit)? = null,
    neutralText: UiText? = null,
    neutralAction: (() -> Unit)? = null,
    itemsSelectionType: ItemsSelectionType = ItemsSelectionType.None,
    preSelectedItems: List<Int> = emptyList(),
    items: List<UiText> = emptyList(),
    onItemClick: ((index: Int) -> Unit)? = null,
    customContent: (@Composable () -> Unit)? = null
) {
    var isVisible by remember {
        mutableStateOf(true)
    }
    val onDismissRequest = {
        onDismissAction.invoke()
        isVisible = false
    }

    val stringTitles = items.map { it.getString().orEmpty() }

    var alertItems by remember {
        mutableStateOf(
            stringTitles.mapIndexed { index, title ->
                DialogItem(
                    title,
                    preSelectedItems.contains(index)
                )
            }
        )
    }

    AppTheme {
        // TODO: 08.04.2023, Danil Nikolaev: implement animation
        AlertAnimation(visible = isVisible) {
            Dialog(onDismissRequest = onDismissRequest) {
                val scrollState = rememberScrollState()
                val canScrollBackward by remember { derivedStateOf { scrollState.value > 0 } }
                val canScrollForward by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AlertDialogDefaults.containerColor,
                    shape = AlertDialogDefaults.shape,
                    tonalElevation = AlertDialogDefaults.TonalElevation
                ) {
                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                        val stringTitle = title?.getString()
                        if (stringTitle != null) {
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Row {
                            stringTitle?.let { title ->
                                Spacer(modifier = Modifier.width(24.dp))
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = title,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                            }
                        }

                        if (canScrollBackward) {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(scrollState)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            val stringMessage = message?.getString()
                            if (stringMessage != null && stringTitle == null) {
                                Spacer(modifier = Modifier.height(20.dp))
                            }

                            Row {
                                stringMessage?.let { message ->
                                    Spacer(modifier = Modifier.width(24.dp))
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(20.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (alertItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                AlertItems(
                                    selectionType = itemsSelectionType,
                                    items = alertItems,
                                    onItemClick = { index ->
                                        onItemClick?.invoke(index)

                                        if (itemsSelectionType == ItemsSelectionType.None) {
                                            onDismissRequest.invoke()
                                        } else {
                                            val newItems =
                                                alertItems.mapIndexed { itemIndex, item ->
                                                    item.copy(isSelected = itemIndex == index)
                                                }

                                            alertItems = newItems
                                        }
                                    },
                                    onItemCheckedChanged = { index ->
                                        val newItems = alertItems.toMutableList()
                                        val oldItem = newItems[index]
                                        newItems[index] =
                                            oldItem.copy(isSelected = !oldItem.isSelected)

                                        alertItems = newItems
                                    }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            } else {
                                customContent?.let { content ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    content.invoke()
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }

                        if (canScrollForward) {
                            Divider(modifier = Modifier.fillMaxWidth())
                        }

                        Row {
                            Spacer(modifier = Modifier.width(20.dp))
                            neutralText?.getString()?.let { text ->
                                TextButton(
                                    onClick = {
                                        onDismissRequest.invoke()
                                        neutralAction?.invoke()
                                    }
                                ) {
                                    Text(text = text)
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            negativeText?.getString()?.let { text ->
                                TextButton(
                                    onClick = {
                                        onDismissRequest.invoke()
                                        negativeAction?.invoke()
                                    }
                                ) {
                                    Text(text = text)
                                }
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            positiveText?.getString()?.let { text ->
                                TextButton(
                                    onClick = {
                                        onDismissRequest.invoke()
                                        positiveAction?.invoke()
                                    }
                                ) {
                                    Text(text = text)
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertAnimation(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                scaleIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(150)),
        content = content
    )
}

@Preview
@Composable
fun AlertItemsPreview() {
    AppTheme {
        AlertItems(
            selectionType = ItemsSelectionType.None,
            items = List(5) { index ->
                DialogItem(
                    title = "Item #${index + 1}",
                    isSelected = index % 2 == 0
                )
            },
            onItemClick = {}
        )
    }
}

@Composable
private fun AlertItems(
    selectionType: ItemsSelectionType,
    items: List<DialogItem>,
    onItemClick: ((index: Int) -> Unit)? = null,
    onItemCheckedChanged: ((index: Int) -> Unit)? = null
) {
    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable {
                    if (selectionType == ItemsSelectionType.Multi) {
                        onItemCheckedChanged?.invoke(index)
                    } else {
                        onItemClick?.invoke(index)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (selectionType) {
                ItemsSelectionType.Multi -> {
                    Spacer(modifier = Modifier.width(10.dp))
                    Checkbox(
                        checked = item.isSelected,
                        onCheckedChange = {}
                    )
                }

                ItemsSelectionType.Single -> {
                    Spacer(modifier = Modifier.width(10.dp))
                    RadioButton(
                        selected = item.isSelected,
                        onClick = {}
                    )
                }

                ItemsSelectionType.None -> {
                    Spacer(modifier = Modifier.width(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

data class DialogItem(
    val title: String,
    val isSelected: Boolean
)

sealed interface ItemsSelectionType {
    data object Single : ItemsSelectionType
    data object Multi : ItemsSelectionType
    data object None : ItemsSelectionType
}
