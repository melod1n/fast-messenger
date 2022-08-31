package com.meloda.fast.screens.chatinfo

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.meloda.fast.R
import com.meloda.fast.api.model.VkChat
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.databinding.FragmentChatInfoMembersBinding
import com.meloda.fast.extensions.dpToPx
import com.meloda.fast.view.SpaceItemDecoration

class ChatInfoMembersFragment :
    BaseViewModelFragment<ChatInfoMembersViewModel>(R.layout.fragment_chat_info_members) {

    companion object {

        private const val ArgProfiles = "profiles"
        private const val ArgGroups = "groups"
        private const val ArgMembers = "members"

        fun newInstance(
            profiles: List<VkUser>,
            groups: List<VkGroup>,
            members: List<VkChat.ChatMember>
        ): ChatInfoMembersFragment {
            val fragment = ChatInfoMembersFragment()
            fragment.arguments = bundleOf(
                ArgProfiles to profiles,
                ArgGroups to groups,
                ArgMembers to members
            )

            return fragment
        }
    }

    override val viewModel: ChatInfoMembersViewModel by viewModels()

    private val binding: FragmentChatInfoMembersBinding by viewBinding()

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.addItemDecoration(SpaceItemDecoration(topMargin = 4.dpToPx()))

        val profiles = requireArguments().getSerializable(ArgProfiles) as List<VkUser>
        val groups = requireArguments().getSerializable(ArgGroups) as List<VkGroup>
        val members = requireArguments().getSerializable(ArgMembers) as List<VkChat.ChatMember>

        val adapter =
            ChatInfoMembersAdapter(
                requireContext(),
                members,
                profiles,
                groups,
                confirmRemoveMemberAction = { memberId ->
                    setFragmentResult(
                        ChatInfoFragment.KeyConfirmRemoveChatUser,
                        bundleOf(ChatInfoFragment.ArgMemberId to memberId)
                    )
                }
            )
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null

        setFragmentResultListener(ChatInfoFragment.KeyRemoveChatUser) { _, bundle ->
            val memberId = bundle.getInt(ChatInfoFragment.ArgMemberId)
            adapter.searchMemberIndex(memberId)?.let { index ->
                adapter.removeAt(index)
            }
        }
    }

}