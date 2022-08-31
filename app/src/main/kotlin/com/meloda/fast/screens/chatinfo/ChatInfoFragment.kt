package com.meloda.fast.screens.chatinfo

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.gone
import com.meloda.fast.extensions.orDots
import com.meloda.fast.extensions.visible
import com.meloda.fast.screens.messages.MessagesHistoryFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ChatInfoFragment : BaseViewModelFragment<ChatInfoViewModel>(R.layout.fragment_chat_info) {

    companion object {
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

    private val binding: FragmentChatInfoBinding by viewBinding()

    private val user: VkUser? by lazy {
        requireArguments().getParcelable(MessagesHistoryFragment.ARG_USER)
    }

    private val group: VkGroup? by lazy {
        requireArguments().getParcelable(MessagesHistoryFragment.ARG_GROUP)
    }

    private val conversation: VkConversation by lazy {
        requireNotNull(requireArguments().getParcelable(MessagesHistoryFragment.ARG_CONVERSATION))
    }

    private val chatMembers: MutableList<VkChat.ChatMember> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (conversation.isChat()) {
            viewModel.getChatInfo(conversation.localId)
        }

        val title = when {
            conversation.isChat() -> conversation.title
            conversation.isUser() -> user?.toString()
            conversation.isGroup() -> group?.name
            else -> null
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

        binding.toolbar.startButtonClickAction = { requireActivity().onBackPressed() }

        binding.viewPager.offscreenPageLimit = getTabsCount() - 1
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            StartProgressEvent -> onProgressStart()
            StopProgressEvent -> onProgressStop()

            is GetChatInfoEvent -> {
                fillChatInfo(event.chat)
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

    private fun fillChatInfo(chat: VkChat) {
        updateStatus(chat)
        chatMembers.addAll(chat.members)
        prepareTabs()
    }

    private fun updateStatus(chat: VkChat? = null) {
        val status = when {
            conversation.isChat() -> {
                val membersCountText = "${conversation.membersCount} members"
                if (chat == null) membersCountText
                else {
                    val onlineMembers = chat.members.filter { it.isOnline == true }
                    "$membersCountText, ${onlineMembers.size} online"
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
            return ChatInfoMembersFragment.newInstance(chatMembers)
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
}