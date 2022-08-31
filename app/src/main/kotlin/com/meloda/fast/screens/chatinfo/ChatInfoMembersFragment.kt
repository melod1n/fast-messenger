package com.meloda.fast.screens.chatinfo

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.meloda.fast.R
import com.meloda.fast.api.model.VkChat
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.databinding.FragmentChatInfoMembersBinding

class ChatInfoMembersFragment :
    BaseViewModelFragment<ChatInfoMembersViewModel>(R.layout.fragment_chat_info_members) {

    companion object {

        private const val ArgMembers = "members"

        fun newInstance(members: List<VkChat.ChatMember>): ChatInfoMembersFragment {
            val fragment = ChatInfoMembersFragment()
            fragment.arguments = bundleOf(
                ArgMembers to members
            )

            return fragment
        }
    }

    override val viewModel: ChatInfoMembersViewModel by viewModels()

    private val binding: FragmentChatInfoMembersBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val members = requireArguments().getSerializable(ArgMembers) as List<VkChat.ChatMember>

        val adapter = ChatInfoMembersAdapter(requireContext(), members)
        binding.recyclerView.adapter = adapter
    }

}