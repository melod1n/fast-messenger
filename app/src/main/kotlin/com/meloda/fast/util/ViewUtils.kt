package com.meloda.fast.util

import android.content.Context
import android.view.View
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
        neutralText: Text? = null,
        neutralAction: (() -> Unit)? = null,
        onDismissAction: (() -> Unit)? = null,
        view: View? = null,
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

        return builder.show()
    }
}
