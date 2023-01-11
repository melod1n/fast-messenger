package com.meloda.fast.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.model.base.Text
import com.meloda.fast.model.base.asString

object ViewUtils {

    fun Context.showDialog(
        title: Text? = null,
        message: Text? = null,
        isCancelable: Boolean = true,
        positiveText: Text? = null,
        positiveAction: (() -> Unit)? = null,
        negativeText: Text? = null,
        negativeAction: (() -> Unit)? = null,
        onDismissAction: (() -> Unit)? = null,
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(this)
            .setCancelable(isCancelable)
            .setOnDismissListener { onDismissAction?.invoke() }

        title?.asString()?.let(builder::setTitle)
        message?.asString()?.let(builder::setMessage)

        positiveText?.let { text ->
            builder.setPositiveButton(text.asString()) { _, _ -> positiveAction?.invoke() }
        }

        negativeText?.let { text ->
            builder.setNegativeButton(text.asString()) { _, _ -> negativeAction?.invoke() }
        }

        return builder.show()
    }
}
