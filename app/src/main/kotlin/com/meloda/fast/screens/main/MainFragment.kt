package com.meloda.fast.screens.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.meloda.fast.activity.MainActivity
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.VkEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseViewModelFragment<MainViewModel>() {

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

        viewModel.checkSession(requireContext())
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        if (event is SetNavBarVisibilityEvent) {
            (requireActivity() as MainActivity).toggleNavBarVisibility(event.isVisible)
        }
    }
}