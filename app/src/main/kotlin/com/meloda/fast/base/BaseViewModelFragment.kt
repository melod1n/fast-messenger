package com.meloda.fast.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.lifecycle.lifecycleScope
import com.meloda.fast.R
import com.meloda.fast.activity.MainActivity
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.IllegalTokenEvent
import com.meloda.fast.base.viewmodel.VkEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

abstract class BaseViewModelFragment<VM : BaseViewModel> : BaseFragment {

    constructor() : super()

    constructor(@LayoutRes resId: Int) : super(resId)

    protected abstract val viewModel: VM

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.onEach { onEvent(it) }.collect()
        }
    }

    protected open fun onEvent(event: VkEvent) {
        if (event is IllegalTokenEvent) {
            Toast.makeText(
                requireContext(), R.string.authorization_failed, Toast.LENGTH_LONG
            ).show()

            UserConfig.clear()
            requireActivity().finishAffinity()
            requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
        }
    }

}