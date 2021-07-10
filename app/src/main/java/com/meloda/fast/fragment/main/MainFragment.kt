package com.meloda.fast.fragment.main

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.viewModels
import com.meloda.fast.R
import com.meloda.fast.base.BaseVMFragment
import com.meloda.fast.databinding.FragmentMainBinding
import com.meloda.fast.extensions.NavigationExtensions.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseVMFragment<MainVM>(R.layout.fragment_main) {

    override val viewModel: MainVM by viewModels()
    private val binding: FragmentMainBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) setupBottomBar()

    }

    private fun setupBottomBar() {
        val navGraphIds = listOf(R.id.messages, R.id.friends)

        binding.bottomBar.setupWithNavController(
            navGraphIds = listOf(),
            fragmentManager = childFragmentManager,
            containerId = R.id.fragmentContainer,
            intent = requireActivity().intent
        )
    }


}