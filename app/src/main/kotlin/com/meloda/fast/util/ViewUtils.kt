package com.meloda.fast.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R

object ViewUtils {

    fun Context.showErrorDialog(
        title: String? = null,
        message: String? = null,
        showErrorPrefix: Boolean = true,
        isCancelable: Boolean = true,
        positiveText: String? = null,
        positiveAction: (() -> Unit)? = null,
        negativeText: String? = null,
        negativeAction: (() -> Unit)? = null,
        onDismissAction: (() -> Unit)? = null,
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(this)
            .setCancelable(isCancelable)
            .setMessage(
                if (showErrorPrefix) getString(R.string.error, message)
                else message
            )
            .setOnDismissListener {
                onDismissAction?.invoke()
            }

        title?.let { text ->
            builder.setTitle(text)
        } ?: run {
            builder.setTitle(R.string.warning)
        }

        positiveText?.let { text ->
            builder.setPositiveButton(text) { _, _ -> positiveAction?.invoke() }
        }

        negativeText?.let { text ->
            builder.setNegativeButton(text) { _, _ -> negativeAction?.invoke() }
        }

        return builder.show()
    }

}
