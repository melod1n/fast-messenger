package ru.melod1n.project.vkm.util

import android.view.View
import ru.melod1n.project.vkm.common.AppGlobal

object KeyboardUtils {

    fun hideKeyboardFrom(view: View) {
        AppGlobal.inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun showKeyboard(focusedView: View) {
        AppGlobal.inputMethodManager.showSoftInput(focusedView, 0)
    }

}