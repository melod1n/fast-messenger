package dev.meloda.fast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.theme.AppTheme
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    title: String? = null,
    text: String? = null,
    selectionType: SelectionType = SelectionType.None,
    items: ImmutableList<String> = ImmutableList.empty(),
    preSelectedItems: ImmutableList<Int> = ImmutableList.empty(),
    onItemClick: ((index: Int) -> Unit)? = null,
    confirmText: String? = null,
    confirmAction: (() -> Unit)? = null,
    confirmContainerColor: Color = MaterialTheme.colorScheme.primary,
    confirmContentColor: Color = MaterialTheme.colorScheme.contentColorFor(confirmContainerColor),
    cancelText: String? = null,
    cancelAction: (() -> Unit)? = null,
    cancelContainerColor: Color = Color.Transparent,
    cancelContentColor: Color = MaterialTheme.colorScheme.contentColorFor(cancelContainerColor),
    neutralText: String? = null,
    neutralAction: (() -> Unit)? = null,
    neutralContainerColor: Color = Color.Transparent,
    neutralContentColor: Color = MaterialTheme.colorScheme.contentColorFor(neutralContainerColor),
    properties: DialogProperties = DialogProperties(),
    actionInvokeDismiss: ActionInvokeDismiss = ActionInvokeDismiss.IfNoAction,
    customContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    var alertItems by remember(items, preSelectedItems) {
        mutableStateOf(
            items.mapIndexed { index, title ->
                DialogItem(
                    title,
                    preSelectedItems.contains(index)
                )
            }
        )
    }

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties
    ) {
        var isPlaced by remember { mutableStateOf(false) }
        val scrollState = rememberScrollState()
        val canScrollBackward by remember { derivedStateOf { scrollState.value > 0 } }
        val canScrollForward by remember { derivedStateOf { scrollState.value < scrollState.maxValue } }
        val shouldAddVerticalPadding = remember(
            icon, title, text, items,
            confirmText, cancelText, neutralText
        ) {
            icon != null || title != null || text != null || items.isNotEmpty() ||
                    confirmText != null || cancelText != null || neutralText != null
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AlertDialogDefaults.containerColor,
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (shouldAddVerticalPadding) {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (title != null) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxWidth(),
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                AnimatedVisibility(isPlaced && canScrollBackward) {
                    HorizontalDivider()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                        .onPlaced { isPlaced = true }
                ) {
                    if (text != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (text != null || title != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (alertItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        AlertItems(
                            selectionType = selectionType,
                            items = alertItems,
                            onItemClick = { index ->
                                if (selectionType == SelectionType.None) {
                                    onDismissRequest.invoke()
                                } else {
                                    val newItems =
                                        alertItems.mapIndexed { itemIndex, item ->
                                            item.copy(isSelected = itemIndex == index)
                                        }

                                    alertItems = newItems
                                }

                                onItemClick?.invoke(index)
                            },
                            onItemCheckedChanged = { index ->
                                val newItems = alertItems.toMutableList()
                                val oldItem = newItems[index]
                                newItems[index] =
                                    oldItem.copy(isSelected = !oldItem.isSelected)

                                alertItems = newItems.toImmutableList()
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    } else {
                        customContent?.invoke(this)
                    }
                }

                AnimatedVisibility(isPlaced && canScrollForward) {
                    HorizontalDivider()
                }

                if (confirmText != null || cancelText != null || neutralText != null) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (confirmText != null) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val hadAction = confirmAction != null
                                    confirmAction?.invoke()

                                    if (actionInvokeDismiss == ActionInvokeDismiss.Always || (actionInvokeDismiss == ActionInvokeDismiss.IfNoAction && !hadAction)) {
                                        onDismissRequest()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = confirmContainerColor,
                                    contentColor = confirmContentColor
                                )
                            ) {
                                Text(text = confirmText)
                            }
                        }

                        if (cancelText != null) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val hadAction = cancelAction != null
                                    cancelAction?.invoke()

                                    if (actionInvokeDismiss == ActionInvokeDismiss.Always || (actionInvokeDismiss == ActionInvokeDismiss.IfNoAction && !hadAction)) {
                                        onDismissRequest()
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = cancelContainerColor,
                                    contentColor = cancelContentColor
                                )
                            ) {
                                Text(text = cancelText)
                            }
                        }

                        if (neutralText != null) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val hadAction = neutralAction != null
                                    neutralAction?.invoke()

                                    if (actionInvokeDismiss == ActionInvokeDismiss.Always || (actionInvokeDismiss == ActionInvokeDismiss.IfNoAction && !hadAction)) {
                                        onDismissRequest()
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = neutralContainerColor,
                                    contentColor = neutralContentColor
                                )
                            ) {
                                Text(text = neutralText)
                            }
                        }
                    }
                }

                if (shouldAddVerticalPadding) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AlertItems(
    selectionType: SelectionType,
    items: ImmutableList<DialogItem>,
    onItemClick: ((index: Int) -> Unit)? = null,
    onItemCheckedChanged: ((index: Int) -> Unit)? = null
) {
    items.forEachIndexed { index, item ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable {
                    if (selectionType == SelectionType.Multi) {
                        onItemCheckedChanged?.invoke(index)
                    } else {
                        onItemClick?.invoke(index)
                    }
                }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (selectionType) {
                SelectionType.Multi -> {
                    Checkbox(
                        checked = item.isSelected,
                        onCheckedChange = {
                            onItemCheckedChanged?.invoke(index)
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                SelectionType.Single -> {
                    RadioButton(
                        selected = item.isSelected,
                        onClick = {
                            onItemClick?.invoke(index)
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                SelectionType.None -> {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
            Text(
                modifier = Modifier.weight(1f),
                text = item.title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

data class DialogItem(
    val title: String,
    val isSelected: Boolean
)

sealed class ActionInvokeDismiss {
    data object Never : ActionInvokeDismiss()
    data object IfNoAction : ActionInvokeDismiss()
    data object Always : ActionInvokeDismiss()
}

sealed class SelectionType {
    data object Single : SelectionType()
    data object Multi : SelectionType()
    data object None : SelectionType()
}

@Preview
@Composable
private fun MaterialDialogPreview() {
    AppTheme {
        MaterialDialog(
            onDismissRequest = {},
            title = "Material Dialog",
            text = "This is a preview of a Material dialog.",
            confirmText = "Confirm",
            cancelText = "Cancel",
            icon = ImageVector.vectorResource(R.drawable.ic_info_round_24)
        )
    }
}

@Preview
@Composable
private fun MaterialDialogWithListPreview() {
    AppTheme {
        MaterialDialog(
            onDismissRequest = {},
            title = "Material Dialog",
            text = "This is a preview of a Material dialog.",
            confirmText = "Confirm",
            cancelText = "Cancel",
            items = listOf("Item 1", "Item 2", "Item 3").toImmutableList(),
            selectionType = SelectionType.Single,
            icon = ImageVector.vectorResource(R.drawable.ic_info_round_24)
        )
    }
}

@Preview
@Composable
private fun MaterialDialogWithCustomContent() {
    AppTheme {
        MaterialDialog(
            onDismissRequest = {},
            title = "Material Dialog",
            confirmText = "Confirm",
            cancelText = "Cancel",
            icon = ImageVector.vectorResource(R.drawable.ic_info_round_24)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .weight(1f),
                    value = "",
                    onValueChange = {},
                    label = { Text(text = "Text") },
                    placeholder = { Text(text = "Text") },
                    shape = RoundedCornerShape(10.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun MaterialDialogWithOnlyCustomContent() {
    AppTheme {
        MaterialDialog(onDismissRequest = {}) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .weight(1f),
                    value = "",
                    onValueChange = {},
                    label = { Text(text = "Text") },
                    placeholder = { Text(text = "Text") },
                    shape = RoundedCornerShape(10.dp),
                )
            }
        }
    }
}
