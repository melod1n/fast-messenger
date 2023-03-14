package com.meloda.fast.ext

import android.os.Build

fun sdkAndUp(sdkInt: Int, action: () -> Unit): Boolean? {
    return if (Build.VERSION.SDK_INT >= sdkInt) {
        action.invoke()
        true
    } else null
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

fun sdk33AndUp(action: () -> Unit): Boolean? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        action.invoke()
        true
    } else null
}
