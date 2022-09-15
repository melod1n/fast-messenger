package com.meloda.fast.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import com.meloda.fast.extensions.doOnApplyWindowInsets
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.util.AndroidUtils

abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    protected var shouldNavBarShown: Boolean = true

    protected var useInsets: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments == null) arguments = Bundle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareInsets()

        (requireActivity() as? MainActivity)?.run {
            toggleNavBarVisibility(shouldNavBarShown)
        }
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

    protected open fun prepareInsets() {
        if (!useInsets) return

        view?.doOnApplyWindowInsets { view, insets, padding ->
            val statusBarInsets = AndroidUtils.getStatusBarInsets(insets)
            val navigationBarInsets = AndroidUtils.getNavBarInsets(insets)

            view.updatePaddingRelative(
                top = padding.top + statusBarInsets.top,
                bottom = padding.bottom + navigationBarInsets.bottom
            )

            insets
        }
    }

}