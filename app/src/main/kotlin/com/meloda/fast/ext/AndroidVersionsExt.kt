package com.meloda.fast.ext

import android.os.Build

fun sdk26AndUp(action: () -> Unit): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        action.invoke()
        true
    } else false
}

fun sdk30AndUp(action: () -> Unit): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        action.invoke()
        true
    } else false
}

fun sdk33AndUp(action: () -> Unit): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        action.invoke()
        true
    } else false
}