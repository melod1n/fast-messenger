package com.meloda.fast.ext

import android.os.Build

fun isSdkAtLeast(sdkInt: Int, action: (() -> Unit)? = null): Boolean {
    return if (Build.VERSION.SDK_INT >= sdkInt) {
        action?.invoke()
        true
    } else {
        false
    }
}

inline fun sdkAndUp(sdkInt: Int, action: () -> Unit): Boolean? {
    return if (Build.VERSION.SDK_INT >= sdkInt) {
        action.invoke()
        true
    } else null
}

fun isSdkAtLeastOr(
    sdkInt: Int,
    action: (() -> Unit)? = null,
    orAction: (() -> Unit)? = null
): Boolean {
    return if (Build.VERSION.SDK_INT >= sdkInt) {
        action?.invoke()
        true
    } else {
        orAction?.invoke()
        false
    }
}

fun sdk26AndUp(action: () -> Unit): Boolean? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        action.invoke()
        true
    } else null
}

fun sdk30AndUp(action: () -> Unit): Boolean? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        action.invoke()
        true
    } else null
}

inline fun sdk33AndUp(crossinline action: () -> Unit): Boolean? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        action.invoke()
        true
    } else null
}
