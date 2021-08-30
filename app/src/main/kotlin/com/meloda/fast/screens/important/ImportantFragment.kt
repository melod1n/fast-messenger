package com.meloda.fast.screens.important

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.databinding.FragmentImportantBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImportantFragment : BaseFragment(R.layout.fragment_important) {

    private val binding: FragmentImportantBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}