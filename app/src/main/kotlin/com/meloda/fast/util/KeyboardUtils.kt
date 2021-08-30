package com.meloda.fast.util

import android.view.View
import com.meloda.fast.common.AppGlobal

object KeyboardUtils {

    fun hideKeyboardFrom(focusedView: View?) {
        AppGlobal.inputMethodManager.hideSoftInputFromWindow(focusedView?.windowToken, 0)
    }

    fun showKeyboard(viewToFocus: View) {
        AppGlobal.inputMethodManager.showSoftInput(viewToFocus, 0)
    }

}