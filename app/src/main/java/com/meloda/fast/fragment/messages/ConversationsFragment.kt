package com.meloda.fast.fragment.messages

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import com.meloda.fast.R
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.databinding.FragmentConversationsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConversationsFragment : BaseFragment(R.layout.fragment_conversations) {

    private val binding: FragmentConversationsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}