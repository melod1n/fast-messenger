package com.meloda.fast.extensions

import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout

object TextViewExtensions {

    fun TextView.clear() {
        text = ""
    }

    fun TextInputLayout.clear() {
        editText?.setText("")
    }


}