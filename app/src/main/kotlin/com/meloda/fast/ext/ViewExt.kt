package com.meloda.fast.ext

import android.view.inputmethod.EditorInfo
import android.widget.EditText

inline fun EditText.onDone(crossinline callback: () -> Unit) {
    imeOptions = EditorInfo.IME_ACTION_DONE
    maxLines = 1
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            return@setOnEditorActionListener true
        }
        false
    }
}

inline fun EditText.onFocusChanged(
    crossinline callback: (editText: EditText, hasFocus: Boolean) -> Unit,
) {
    setOnFocusChangeListener { v, hasFocus ->

    }
}
