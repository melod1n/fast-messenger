package com.meloda.fast.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.meloda.fast.screens.main.MainActivity

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments == null) arguments = Bundle()
    }

    val activityRouter
        get() = run {
            if (requireActivity() is MainActivity) {
                (requireActivity() as MainActivity).router
            } else {
                null
            }
        }

    fun requireActivityRouter() = requireNotNull(activityRouter)

    fun startProgress() = toggleProgress(true)
    fun stopProgress() = toggleProgress(false)

    protected open fun toggleProgress(isProgressing: Boolean) {}

}
