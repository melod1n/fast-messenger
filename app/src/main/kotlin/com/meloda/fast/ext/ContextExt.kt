package com.meloda.fast.ext

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.asString

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
    itemsChoiceType: ItemsChoiceType = ItemsChoiceType.None,
    itemsClickAction: ((index: Int, value: String) -> Unit)? = null,
    itemsMultiChoiceClickAction: ((index: Int, value: String, isChecked: Boolean) -> Unit)? = null,
    checkedItems: List<Int>? = null
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
        when (itemsChoiceType) {
            ItemsChoiceType.None -> {
                builder.setItems(
                    stringItems.toTypedArray()
                ) { dialog, which ->
                    dialog.dismiss()
                    itemsClickAction?.invoke(which, stringItems[which])
                }
            }
            ItemsChoiceType.SingleChoice -> {
                builder.setSingleChoiceItems(
                    stringItems.toTypedArray(),
                    checkedItems?.first() ?: -1
                ) { _, which ->
                    itemsClickAction?.invoke(which, stringItems[which])
                }
            }
            ItemsChoiceType.MultiChoice -> {
                builder.setMultiChoiceItems(
                    stringItems.toTypedArray(),
                    BooleanArray(stringItems.size) { index -> checkedItems?.contains(index).isTrue }
                ) { _, which, isChecked ->
                    itemsMultiChoiceClickAction?.invoke(which, stringItems[which], isChecked)
                }
            }
        }
    }

    return builder.show()
}

sealed class ItemsChoiceType {
    object None : ItemsChoiceType()
    object SingleChoice : ItemsChoiceType()
    object MultiChoice : ItemsChoiceType()
}
