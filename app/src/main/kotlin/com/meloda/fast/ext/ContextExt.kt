package com.meloda.fast.ext

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.compose.ItemsSelectionType
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString

@Deprecated("Migrate to compose dialogs")
fun Context.showDialog(
    title: UiText? = null,
    message: UiText? = null,
    isCancelable: Boolean = true,
    positiveText: UiText? = null,
    positiveAction: (() -> Unit)? = null,
    negativeText: UiText? = null,
    negativeAction: (() -> Unit)? = null,
    neutralText: UiText? = null,
    neutralAction: (() -> Unit)? = null,
    onDismissAction: (() -> Unit)? = null,
    view: View? = null,
    items: List<UiText>? = null,
    itemsSelectionType: ItemsSelectionType = ItemsSelectionType.None,
    onItemClick: ((index: Int, value: String) -> Unit)? = null,
    itemsMultiChoiceClickAction: ((index: Int, value: String, isChecked: Boolean) -> Unit)? = null,
    preSelectedItems: List<Int>? = null
): AlertDialog {
    val builder = MaterialAlertDialogBuilder(this)
        .setCancelable(isCancelable)
        .setOnDismissListener { onDismissAction?.invoke() }

    title?.asString()?.let(builder::setTitle)
    message?.asString()?.let(builder::setMessage)

    view?.let(builder::setView)

    positiveText?.let { text ->
        builder.setPositiveButton(text.asString()) { _, _ -> positiveAction?.invoke() }
    }
    negativeText?.let { text ->
        builder.setNegativeButton(text.asString()) { _, _ -> negativeAction?.invoke() }
    }
    neutralText?.let { text ->
        builder.setNeutralButton(text.asString()) { _, _ -> neutralAction?.invoke() }
    }

    items?.mapNotNull { it.asString() }?.let { stringItems ->
        when (itemsSelectionType) {
            ItemsSelectionType.None -> {
                builder.setItems(
                    stringItems.toTypedArray()
                ) { dialog, which ->
                    dialog.dismiss()
                    onItemClick?.invoke(which, stringItems[which])
                }
            }

            ItemsSelectionType.Single -> {
                builder.setSingleChoiceItems(
                    stringItems.toTypedArray(),
                    preSelectedItems?.first() ?: -1
                ) { _, which ->
                    onItemClick?.invoke(which, stringItems[which])
                }
            }

            ItemsSelectionType.Multi -> {
                builder.setMultiChoiceItems(
                    stringItems.toTypedArray(),
                    BooleanArray(stringItems.size) { index -> preSelectedItems?.contains(index).isTrue }
                ) { _, which, isChecked ->
                    itemsMultiChoiceClickAction?.invoke(which, stringItems[which], isChecked)
                }
            }
        }
    }

    return builder.show()
}

context(Context)
fun UiText?.asString(): String? {
    return this.parseString(this@Context)
}
