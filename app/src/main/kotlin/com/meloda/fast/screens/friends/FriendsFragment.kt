package com.meloda.fast.screens.friends

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.databinding.FragmentFriendsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsFragment : BaseFragment(R.layout.fragment_friends) {

    private val binding: FragmentFriendsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}