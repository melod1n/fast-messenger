package com.meloda.fast.util

import android.view.View
import com.meloda.fast.common.AppGlobal

object KeyboardUtils {

    fun hideKeyboardFrom(view: View) {
        AppGlobal.inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(focusedView: View) {
        AppGlobal.inputMethodManager.showSoftInput(focusedView, 0)
    }

}