package com.meloda.fast.screens.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.viewModels
import com.google.android.material.textview.MaterialTextView
import com.meloda.fast.R
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.databinding.FragmentMainBinding
import com.meloda.fast.extensions.setupWithNavController
import com.meloda.fast.service.MessagesUpdateService
import com.meloda.fast.service.OnlineService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : BaseViewModelFragment<MainViewModel>(R.layout.fragment_main) {

    override val viewModel: MainViewModel by viewModels()
    private val binding: FragmentMainBinding by viewBinding()

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupBottomBar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (!UserConfig.isLoggedIn()) findNavController().navigate(R.id.toLogin)

        requireContext().run {
            startService(Intent(this, MessagesUpdateService::class.java))
            startService(Intent(this, OnlineService::class.java))
        }
        if (savedInstanceState == null) {
            setupBottomBar()
        }
    }

    private fun setupBottomBar() {
        val navGraphIds = listOf(
            R.navigation.messages
        )

        binding.bottomBar.run {
            setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = childFragmentManager,
                containerId = R.id.childFragmentContainer,
                intent = requireActivity().intent
            )
        }
    }

}