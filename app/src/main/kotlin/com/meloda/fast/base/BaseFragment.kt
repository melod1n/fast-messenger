package com.meloda.fast.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.meloda.fast.screens.main.MainActivity

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    @Deprecated("use DI")
    val activityRouter get() = (activity as? MainActivity)?.accessRouter()

    @Deprecated("use isInProgress Flow in VM")
    fun startProgress() = toggleProgress(true)

    @Deprecated("use isInProgress Flow in VM")
    fun stopProgress() = toggleProgress(false)

    @Deprecated("use isInProgress Flow in VM")
    protected open fun toggleProgress(isProgressing: Boolean) {
    }
}
