package com.meloda.fast.ext

import android.app.Activity
import androidx.core.view.WindowCompat

fun Activity.edgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}