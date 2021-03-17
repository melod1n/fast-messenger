package com.meloda.fast.base

import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.meloda.fast.activity.MainActivity

abstract class BaseFragment : Fragment() {

    protected open fun initToolbar(@IdRes resId: Int) {
        val toolbar: Toolbar = requireView().findViewById(resId)

        activity?.let {
            if (it is MainActivity && toolbar is com.meloda.fast.widget.Toolbar) it.initToolbar(
                toolbar
            )
        }
    }

    fun runOnUiThread(runnable: Runnable) {
        activity?.runOnUiThread(runnable)
    }
}