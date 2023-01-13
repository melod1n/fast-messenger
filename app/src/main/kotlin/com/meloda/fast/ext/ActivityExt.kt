package com.meloda.fast.ext

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun Activity.edgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

context(AppCompatActivity)
fun <T> Flow<T>.listenValue(action: suspend (T) -> Unit): Job {
    return onEach {
        action.invoke(it)
    }.launchIn(lifecycleScope)
}
