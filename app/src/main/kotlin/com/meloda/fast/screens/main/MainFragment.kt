package com.meloda.fast.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.viewmodel.ViewModelUtils
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.ext.listenValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseFragment() {

    companion object {
        const val START_SERVICES_KEY = "start_services"
        const val START_SERVICES_ARG_ENABLE = "enable"
    }

    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = View(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenViewModel()
    }

    private fun listenViewModel() {
        viewModel.tasksEvent.listenValue(::onEvent)

        viewModel.servicesState.listenValue { state ->
            val enableServices = state == ServicesState.Started
            setFragmentResult(
                START_SERVICES_KEY,
                bundleOf(START_SERVICES_ARG_ENABLE to enableServices)
            )
        }
    }

    private fun onEvent(event: VkEvent) {
        ViewModelUtils.parseEvent(this, event)
    }
}
