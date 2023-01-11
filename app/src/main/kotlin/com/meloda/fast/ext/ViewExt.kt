package com.meloda.fast.ext

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.MutableLiveData
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.meloda.fast.common.AppGlobal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

val EditText.trimmedText: String get() = text.toString().trim()
fun EditText.selectLast() {
    setSelection(text.length)
}

fun EditText.notifyObservers() {
    this.text = this.text
}

fun EditText.notifyAboutChanges(mutableLiveData: MutableLiveData<String>) {
    doAfterTextChanged { editable ->
        mutableLiveData.value = editable?.toString().orEmpty()
    }
}

fun EditText.notifyAboutChanges(stateFlow: MutableStateFlow<String>) {
    doAfterTextChanged { editable ->
        val text = editable?.toString().orEmpty()
        stateFlow.update { text }
    }
}

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

fun View.showKeyboard(flags: Int = 0) {
    AppGlobal.inputMethodManager.showSoftInput(this, flags)
}

fun View.hideKeyboard(focusedView: View? = null, flags: Int = 0) {
    AppGlobal.inputMethodManager.hideSoftInputFromWindow(
        focusedView?.windowToken ?: this.windowToken, flags
    )
}

fun TextInputLayout.clearError() {
    if (error != null) error = null
}

fun TextInputLayout.clearTextOnErrorIconClick(textField: TextInputEditText) {
    setErrorIconOnClickListener {
        textField.text = null
        textField.showKeyboard()
    }
}
