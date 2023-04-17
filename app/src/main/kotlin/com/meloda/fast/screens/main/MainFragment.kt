package com.meloda.fast.screens.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.viewmodel.ViewModelUtils
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.ext.listenValue
import com.meloda.fast.screens.main.activity.ServicesState
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment() {

    private val viewModel: MainViewModel by viewModel<MainViewModelImpl>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainFragment", "onCreate: viewModel: $viewModel")
    }

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
        viewModel.events.listenValue(::onEvent)

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

    companion object {
        const val START_SERVICES_KEY = "start_services"
        const val START_SERVICES_ARG_ENABLE = "enable"

        fun newInstance(): MainFragment = MainFragment()
    }
}
