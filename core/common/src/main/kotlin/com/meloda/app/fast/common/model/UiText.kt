package com.meloda.app.fast.common.model

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed class UiText {

    data object Empty : UiText()

    data class Resource(@StringRes val resId: Int) : UiText()

    data class ResourceParams(
        @StringRes val value: Int,
        val args: List<Any?>,
    ) : UiText()

    data class Simple(val text: String) : UiText()

    data class QuantityResource(@PluralsRes val resId: Int, val quantity: Int) : UiText()


}

fun UiText?.parseString(resources: Resources): String? {
    return when (this) {
        is UiText.Resource -> resources.getString(resId)
        is UiText.ResourceParams -> {
            val processedArgs = args.map { any ->
                when (any) {
                    is UiText -> any.parseString(resources)
                    else -> any
                }
            }
            resources.getString(value, *processedArgs.toTypedArray())
        }

        is UiText.QuantityResource -> resources.getQuantityString(resId, quantity, quantity)
        is UiText.Simple -> text
        else -> null
    }
}
