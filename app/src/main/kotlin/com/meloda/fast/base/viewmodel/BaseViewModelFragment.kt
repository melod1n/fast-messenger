package com.meloda.fast.base.viewmodel

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import com.meloda.fast.base.BaseFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class BaseViewModelFragment<VM : BaseViewModel> : BaseFragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    protected abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel(viewModel)
    }

    protected open fun onEvent(event: VkEvent) {
        ViewModelUtils.parseEvent(this, event)
    }

    protected fun <T : BaseViewModel> subscribeToViewModel(viewModel: T) {
        lifecycleScope.launch {
            viewModel.tasksEvent.collect { onEvent(it) }
        }
    }

}