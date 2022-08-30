package com.meloda.fast.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.VkEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseViewModelFragment<MainViewModel>() {

    companion object {
        const val KeyStartServices = "start_services"
    }

    override val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.checkSession()
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            StartServicesEvent -> {
                setFragmentResult(KeyStartServices, bundleOf("enable" to true))
            }
            StopServicesEvent -> {
                setFragmentResult(KeyStartServices, bundleOf("enable" to false))
            }
            is SetNavBarVisibilityEvent -> {
                (requireActivity() as MainActivity).toggleNavBarVisibility(event.isVisible)
            }
        }
    }
}