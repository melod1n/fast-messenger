package com.meloda.fast.screens.chatinfo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.meloda.fast.R
import com.meloda.fast.api.model.VkChat
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.databinding.FragmentChatInfoBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.orDots
import com.meloda.fast.ext.visible
import com.meloda.fast.screens.messages.MessagesHistoryFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ChatInfoFragment : BaseViewModelFragment<ChatInfoViewModel>(R.layout.fragment_chat_info) {

    companion object {
        const val KeyConfirmRemoveChatUser = "confirm_remove_chat_user"
        const val KeyRemoveChatUser = "remove_chat_user"
        const val ArgMemberId = "member_id"

        private const val ArgConversation = "conversation"
        private const val ArgUser = "user"
        private const val ArgGroup = "group"

        fun newInstance(
            conversation: VkConversation,
            user: VkUser?,
            group: VkGroup?
        ): ChatInfoFragment {
            val fragment = ChatInfoFragment()
            fragment.arguments = bundleOf(
                ArgConversation to conversation,
                ArgUser to user,
                ArgGroup to group
            )

            return fragment
        }
    }

    override val viewModel: ChatInfoViewModel by viewModels()

    private val binding by viewBinding(FragmentChatInfoBinding::bind)

    private val user: VkUser? by lazy {
        requireArguments().getParcelableCompat(MessagesHistoryFragment.ARG_USER, VkUser::class.java)
    }

    private val group: VkGroup? by lazy {
        requireArguments().getParcelableCompat(
            MessagesHistoryFragment.ARG_GROUP,
            VkGroup::class.java
        )
    }

    private val conversation: VkConversation by lazy {
        requireNotNull(
            requireArguments().getParcelableCompat(
                MessagesHistoryFragment.ARG_CONVERSATION,
                VkConversation::class.java
            )
        )
    }

    private val chatProfiles: MutableList<VkUser> = mutableListOf()
    private val chatGroups: MutableList<VkGroup> = mutableListOf()
    private val chatMembers: MutableList<VkChat.ChatMember> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getConversationMembers(conversation.id)

        val title = when {
            conversation.isChat() -> conversation.title
            conversation.isUser() -> user?.toString()
            conversation.isGroup() -> group?.name
            else -> null
        }


        binding.toolbar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.progresBar.applyInsetter {
            type(navigationBars = true) { padding() }
        }
        binding.toolbar.title = title.orDots()

        updateStatus()

        val avatar = when {
            conversation.isUser() -> user?.photo200
            conversation.isGroup() -> group?.photo200
            conversation.isChat() -> conversation.photo200
            else -> null
        }

        val avatarImageView = binding.toolbar.avatarImageView
        avatarImageView.visible()
        avatarImageView.loadWithGlide(url = avatar, asCircle = true, crossFade = true)

        binding.toolbar.avatarClickAction = {
            showAvatarOptions()
        }
        binding.toolbar.startButtonClickAction = { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.viewPager.offscreenPageLimit = getTabsCount() - 1

        childFragmentManager.setFragmentResultListener(
            KeyConfirmRemoveChatUser,
            this
        ) { _, bundle ->
            val memberId = bundle.getInt(ArgMemberId)
            showConfirmRemoveMemberAlert(memberId)
        }
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            StartProgressEvent -> onProgressStart()
            StopProgressEvent -> onProgressStop()

            is GetConversationMembersEvent -> {
                fillChatInfo(event)
            }
            is RemoveChatUserEvent -> {
                val memberId = event.memberId
                childFragmentManager.setFragmentResult(
                    KeyRemoveChatUser, bundleOf(
                        ArgMemberId to memberId
                    )
                )
            }
        }
    }

    private fun onProgressStart() {
        binding.tabs.gone()
        binding.viewPager.gone()
        binding.progresBar.visible()
    }

    private fun onProgressStop() {
        binding.tabs.visible()
        binding.viewPager.visible()
        binding.progresBar.gone()
    }

    private fun fillChatInfo(event: GetConversationMembersEvent) {
        val onlineMembers = event.profiles.filter { it.online }
        updateStatus(onlineMembers.size)

        val eventChatMembers = event.items.map { vkChatMember ->
            val memberUser: VkUser? = if (vkChatMember.memberId < 0) null
            else event.profiles.firstOrNull { it.id == vkChatMember.memberId }

            val memberGroup: VkGroup? = if (vkChatMember.memberId > 0) null
            else event.groups.firstOrNull { it.id == vkChatMember.memberId }

            VkChat.ChatMember(
                id = vkChatMember.memberId,
                type = if (vkChatMember.memberId > 0) VkChat.ChatMember.ChatMemberType.Profile else VkChat.ChatMember.ChatMemberType.Group,
                isOnline = memberUser?.online,
                lastSeen = memberUser?.lastSeen,
                name = memberGroup?.name,
                firstName = memberUser?.firstName,
                lastName = memberUser?.lastName,
                invitedBy = vkChatMember.invitedBy,
                photo50 = null,
                photo100 = null,
                photo200 = memberUser?.photo200 ?: memberGroup?.photo200,
                isOwner = vkChatMember.isOwner,
                isAdmin = vkChatMember.isAdmin,
                canKick = vkChatMember.canKick
            )
        }

        chatProfiles.addAll(event.profiles)
        chatGroups.addAll(event.groups)
        chatMembers.addAll(eventChatMembers)
        prepareTabs()
    }

    private fun updateStatus(onlineMembersCount: Int? = null) {
        val status = when {
            conversation.isChat() -> {
                val membersCountText = "${conversation.membersCount} members"
                if (onlineMembersCount == null) membersCountText
                else {
                    "$membersCountText, $onlineMembersCount online"
                }
            }
            conversation.isUser() -> when {
                // TODO: 9/15/2021 user normal time
                user?.online == true -> "Online"
                user?.lastSeen != null -> "Last seen at ${
                    SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(user?.lastSeen!! * 1000L)
                }"
                else -> if (user?.lastSeenStatus != null) "Last seen ${user?.lastSeenStatus!!}" else "Last seen recently"
            }
            conversation.isGroup() -> if (group?.membersCount != null) "${group?.membersCount} members" else "Group"
            else -> null
        }

        binding.toolbar.subtitle = status.orDots()

    }

    fun getTabsCount(): Int {
        return if (conversation.isChat()) 6 else 5
    }

    fun createTabFragment(position: Int): Fragment {
        if (conversation.isChat() && position == 0) {
            return ChatInfoMembersFragment.newInstance(
                chatProfiles,
                chatGroups,
                chatMembers
            )
        }

        return Fragment()
    }

    private fun prepareTabs() {
        val titles = mutableListOf("Members", "Photos", "Videos", "Audios", "Files", "Links")

        if (!conversation.isChat()) {
            titles.removeAt(0)
        }

        binding.viewPager.adapter = ChatInfoPagerAdapter(this)

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun showConfirmRemoveMemberAlert(memberId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.warning)
            .setMessage(R.string.confirm_remove_chat_user)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.removeChatUser(conversation.localId, memberId)
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun showAvatarOptions() {
        val options = mutableListOf("Open")

        if (conversation.canChangeInfo) {
            options += listOf("Edit", "Delete")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Open" -> {
                        Toast.makeText(requireContext(), "Open photo", Toast.LENGTH_SHORT).show()
                    }
                    else ->
                        Toast.makeText(requireContext(), "Change info", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }
}