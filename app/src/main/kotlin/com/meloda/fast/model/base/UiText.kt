package com.meloda.fast.model.base

import android.content.Context
import android.os.Parcelable
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
sealed class UiText : Parcelable {

    data class Resource(@StringRes val resId: Int) : UiText()

    data class ResourceParams(
        @StringRes val value: Int,
        val args: List<@RawValue Any?>,
    ) : UiText()

    data class Simple(val text: String) : UiText()

    data class QuantityResource(@PluralsRes val resId: Int, val quantity: Int) : UiText()
}

context(Context)
fun UiText?.asString(): String? {
    return this.asString(this@Context)
}

context(Fragment)
fun UiText?.asString(): String? {
    return this.asString(this@Fragment.requireContext())
}

@Composable
fun UiText?.asString(): String? {
    return this.asString(LocalContext.current)
}

fun UiText?.asString(context: Context): String? {
    return when (this) {
        is UiText.Resource -> context.getString(resId)
        is UiText.ResourceParams -> context.getString(value, *args.toTypedArray())
        is UiText.QuantityResource -> context.resources.getQuantityString(resId, quantity, quantity)
        is UiText.Simple -> text
        else -> null
    }
}
