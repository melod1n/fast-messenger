package com.meloda.fast.model.base

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import kotlinx.parcelize.RawValue

sealed class UiText {

    data object Empty : UiText()

    data class Resource(@StringRes val resId: Int) : UiText()

    data class ResourceParams(
        @StringRes val value: Int,
        val args: List<@RawValue Any?>,
    ) : UiText()

    data class Simple(val text: String) : UiText()

    data class Annotated(val string: AnnotatedString) : UiText()

    data class QuantityResource(@PluralsRes val resId: Int, val quantity: Int) : UiText()
}

fun UiText?.parseString(context: Context): String? {
    return when (this) {
        is UiText.Resource -> context.getString(resId)
        is UiText.ResourceParams -> {
            val processedArgs = args.map { any ->
                when (any) {
                    is UiText -> any.parseString(context)
                    else -> any
                }
            }
            context.getString(value, *processedArgs.toTypedArray())
        }

        is UiText.QuantityResource -> context.resources.getQuantityString(resId, quantity, quantity)
        is UiText.Simple -> text
        else -> null
    }
}
