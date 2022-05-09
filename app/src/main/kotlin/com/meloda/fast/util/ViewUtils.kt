package com.meloda.fast.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R

object ViewUtils {

    fun Context.showErrorDialog(
        message: String,
        showErrorPrefix: Boolean = true,
        isCancelable: Boolean? = null,
        positiveText: Int? = null,
        positiveAction: (() -> Unit)? = null,
        negativeText: Int? = null,
        negativeAction: (() -> Unit)? = null,
    ): AlertDialog {
        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.warning)
            .setMessage(
                if (showErrorPrefix) getString(R.string.error, message)
                else message
            )
            .setPositiveButton(positiveText ?: android.R.string.ok) { _, _ ->
                positiveAction?.invoke()
            }

        negativeAction?.run {
            builder.setNegativeButton(
                negativeText ?: android.R.string.cancel
            ) { _, _ -> this.invoke() }
        }

        isCancelable?.run { builder.setCancelable(this) }

        return builder.show()
    }

}