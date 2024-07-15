package com.meloda.app.fast.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.parseString
import com.meloda.app.fast.designsystem.ImmutableList.Companion.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String? = null,
    confirmAction: (() -> Unit)? = null,
    cancelText: String? = null,
    cancelAction: (() -> Unit)? = null,
    neutralText: String? = null,
    neutralAction: (() -> Unit)? = null,
    title: String? = null,
    text: String? = null,
    itemsSelectionType: ItemsSelectionType = ItemsSelectionType.None,
    items: ImmutableList<String> = ImmutableList.empty(),
    preSelectedItems: ImmutableList<Int> = ImmutableList.empty(),
    onItemClick: ((index: Int) -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    actionInvokeDismiss: ActionInvokeDismiss = ActionInvokeDismiss.IfNoAction,
    customContent: (ColumnScope.() -> Unit)? = null
) {
    var alertItems by remember {
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
                if (title != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Row {
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
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    if (text != null && title == null) {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (text != null) {
                        Row {
                            Spacer(modifier = Modifier.width(24.dp))
                            Text(
                                modifier = Modifier.weight(1f),
                                text = text,
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

                                alertItems = newItems.toImmutableList()
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        if (customContent != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            customContent.invoke(this)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                if (canScrollForward) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth())
                }

                Row {
                    Spacer(modifier = Modifier.width(20.dp))
                    if (neutralText != null) {
                        TextButton(
                            onClick = {
                                neutralAction?.invoke() ?: kotlin.run {
                                    if (actionInvokeDismiss == ActionInvokeDismiss.IfNoAction) {
                                        onDismissRequest()
                                    }
                                }

                                if (actionInvokeDismiss == ActionInvokeDismiss.Always) {
                                    onDismissRequest()
                                }
                            }
                        ) {
                            Text(text = neutralText)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (cancelText != null) {
                        TextButton(
                            onClick = {
                                cancelAction?.invoke() ?: kotlin.run {
                                    if (actionInvokeDismiss == ActionInvokeDismiss.IfNoAction) {
                                        onDismissRequest()
                                    }
                                }

                                if (actionInvokeDismiss == ActionInvokeDismiss.Always) {
                                    onDismissRequest()
                                }
                            }
                        ) {
                            Text(text = cancelText)
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    if (confirmText != null) {
                        TextButton(
                            onClick = {
                                confirmAction?.invoke() ?: kotlin.run {
                                    if (actionInvokeDismiss == ActionInvokeDismiss.IfNoAction) {
                                        onDismissRequest()
                                    }
                                }

                                if (actionInvokeDismiss == ActionInvokeDismiss.Always) {
                                    onDismissRequest()
                                }
                            }
                        ) {
                            Text(text = confirmText)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }
    }
}

// TODO: 08.04.2023, Danil Nikolaev: refactor this
@Deprecated("need refactoring")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDialog(
    onDismissAction: (() -> Unit),
    title: UiText? = null,
    text: UiText? = null,
    confirmText: UiText? = null,
    confirmAction: (() -> Unit)? = null,
    cancelText: UiText? = null,
    cancelAction: (() -> Unit)? = null,
    neutralText: UiText? = null,
    neutralAction: (() -> Unit)? = null,
    itemsSelectionType: ItemsSelectionType = ItemsSelectionType.None,
    preSelectedItems: ImmutableList<Int> = ImmutableList.empty(),
    items: ImmutableList<UiText> = ImmutableList.empty(),
    onItemClick: ((index: Int) -> Unit)? = null,
    buttonsInvokeDismiss: Boolean = true,
    properties: DialogProperties = DialogProperties(),
    customContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    var isVisible by rememberSaveable {
        mutableStateOf(true)
    }
    val onDismissRequest = remember {
        {
            onDismissAction.invoke()
            isVisible = false
        }
    }

    val context = LocalContext.current

    val stringTitles = items.mapNotNull { it.parseString(context.resources) }

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

    if (isVisible) {
        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            properties = properties
        ) {
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
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        val stringMessage = text?.getString()
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

                                    alertItems = newItems.toImmutableList()
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        } else {
                            customContent?.let { content ->
                                Spacer(modifier = Modifier.height(4.dp))
                                content.invoke(this)
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }

                    if (canScrollForward) {
                        HorizontalDivider(modifier = Modifier.fillMaxWidth())
                    }

                    Row {
                        Spacer(modifier = Modifier.width(20.dp))
                        neutralText?.getString()?.let { text ->
                            TextButton(
                                onClick = {
                                    if (buttonsInvokeDismiss) {
                                        onDismissRequest.invoke()
                                    } else {
                                        isVisible = false
                                    }
                                    neutralAction?.invoke()
                                }
                            ) {
                                Text(text = text)
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        cancelText?.getString()?.let { text ->
                            TextButton(
                                onClick = {
                                    if (buttonsInvokeDismiss) {
                                        onDismissRequest.invoke()
                                    } else {
                                        isVisible = false
                                    }
                                    cancelAction?.invoke()
                                }
                            ) {
                                Text(text = text)
                            }
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        confirmText?.getString()?.let { text ->
                            TextButton(
                                onClick = {
                                    if (buttonsInvokeDismiss) {
                                        onDismissRequest.invoke()
                                    } else {
                                        isVisible = false
                                    }
                                    confirmAction?.invoke()
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

@Composable
private fun AlertItems(
    selectionType: ItemsSelectionType,
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
                    if (selectionType == ItemsSelectionType.Multi) {
                        onItemCheckedChanged?.invoke(index)
                    } else {
                        onItemClick?.invoke(index)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TODO: 29/12/2023, Danil Nikolaev: check onClick & onCheckedChange actions
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

sealed class ActionInvokeDismiss {
    data object Never : ActionInvokeDismiss()
    data object IfNoAction : ActionInvokeDismiss()
    data object Always : ActionInvokeDismiss()
}

sealed class ItemsSelectionType {
    data object Single : ItemsSelectionType()
    data object Multi : ItemsSelectionType()
    data object None : ItemsSelectionType()
}
