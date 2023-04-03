package com.meloda.fast.ext

import android.content.Context
import android.widget.Toast

inline fun String?.ifEmpty(defaultValue: () -> String?): String? =
    if (this?.isEmpty() == true) defaultValue() else this

fun String?.orDots(count: Int = 3): String {
    return this ?: ("." * count)
}

operator fun String.times(count: Int): String {
    val builder = StringBuilder()
    for (i in 0 until count) {
        builder.append(this)
    }

    return builder.toString()
}

fun String.toast(context: Context, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(context, this, duration).show()
}

context (Context)
fun String.toast(duration: Int = Toast.LENGTH_LONG) = toast(this@Context, duration)
