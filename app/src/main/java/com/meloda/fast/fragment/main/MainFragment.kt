package com.meloda.fast.fragment.main

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.meloda.fast.R
import com.meloda.fast.UserConfig
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

        if (!UserConfig.isLoggedIn()) findNavController().navigate(R.id.toLogin)
    }

    private fun setupBottomBar() {
        val navGraphIds = listOf(
            R.navigation.messages,
            R.navigation.friends,
            R.navigation.important,
            R.navigation.login
        )

        with(binding.bottomBar) {
            selectedItemId = R.id.messages
            setupWithNavController(
                navGraphIds = navGraphIds,
                fragmentManager = childFragmentManager,
                containerId = R.id.fragmentContainer,
                intent = requireActivity().intent
            )
        }
    }

    val bottomBar get() = binding.bottomBar


}