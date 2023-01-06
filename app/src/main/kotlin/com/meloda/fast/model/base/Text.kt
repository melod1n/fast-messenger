package com.meloda.fast.model.base

import android.content.Context
import android.text.SpannableString
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

sealed class Text {

    data class Resource(@StringRes val resId: Int) : Text()

    data class ResourceParams(
        @StringRes val value: Int,
        val args: List<Any>,
    ) : Text()

    data class Simple(val text: String) : Text()

    data class QuantityResource(@PluralsRes val resId: Int, val quantity: Int) : Text()
}

context(Context)
fun Text?.asString(): String? {
    return this.asString(this@Context)
}

context(Fragment)
fun Text?.asString(): String? {
    return this.asString(this@Fragment.requireContext())
}

fun Text?.asString(context: Context): String? {
    return when (this) {
        is Text.Resource -> context.getString(resId)
        is Text.ResourceParams -> context.getString(value, args)
        is Text.QuantityResource -> context.resources.getQuantityString(resId, quantity, quantity)
        is Text.Simple -> text
        else -> null
    }
}
