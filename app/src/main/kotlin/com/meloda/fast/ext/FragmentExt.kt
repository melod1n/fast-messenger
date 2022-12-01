package com.meloda.fast.ext

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

context(Fragment)
fun <T> Flow<T>.listenValue(action: suspend (T) -> Unit) {
    onEach {
        action.invoke(it)
    }.launchIn(viewLifecycleOwner.lifecycleScope)
}

context(Fragment)
fun String.toast(duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(requireContext(), this, duration).show()
}