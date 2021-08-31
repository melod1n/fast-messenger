package com.meloda.fast.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VKEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

abstract class BaseVMFragment<VM : BaseViewModel> : BaseFragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    protected abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach { onEvent(it) }.collect()
        }
    }

    protected open fun onEvent(event: VKEvent) {}

}