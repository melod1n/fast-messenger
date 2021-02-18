package ru.melod1n.project.vkm.base

import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import ru.melod1n.project.vkm.activity.MainActivity

abstract class BaseFragment : Fragment() {

    protected open fun initToolbar(@IdRes resId: Int) {
        val toolbar: Toolbar = requireView().findViewById(resId)

        activity?.let {
            if (it is MainActivity && toolbar is ru.melod1n.project.vkm.widget.Toolbar)  it.initToolbar(toolbar)
        }
    }

    protected fun runOnUi(runnable: Runnable) {
        activity?.runOnUiThread(runnable)
    }
}