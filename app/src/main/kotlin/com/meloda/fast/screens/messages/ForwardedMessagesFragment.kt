package com.meloda.fast.screens.messages

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.os.bundleOf
import com.meloda.fast.R
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentForwardedMessagesBinding

class ForwardedMessagesFragment : BaseFragment(R.layout.fragment_forwarded_messages) {

    companion object {
        private const val ArgConversation = "conversation"
        private const val ArgMessages = "messages"
        private const val ArgProfiles = "profiles"
        private const val ArgGroups = "groups"

        fun newInstance(
            conversation: VkConversation,
            messages: List<VkMessage>,
            profiles: HashMap<Int, VkUser> = hashMapOf(),
            groups: HashMap<Int, VkGroup> = hashMapOf()
        ): ForwardedMessagesFragment {
            val fragment = ForwardedMessagesFragment()
            fragment.arguments = bundleOf(
                ArgConversation to conversation,
                ArgMessages to messages,
                ArgProfiles to profiles,
                ArgGroups to groups
            )

            return fragment
        }
    }

    private val binding: FragmentForwardedMessagesBinding by viewBinding()

    private var conversation: VkConversation? = null
    private var messages: List<VkMessage> = emptyList()
    private var profiles: HashMap<Int, VkUser> = hashMapOf()
    private var groups: HashMap<Int, VkGroup> = hashMapOf()

    private val adapter: MessagesHistoryAdapter by lazy {
        MessagesHistoryAdapter(
            this, requireNotNull(conversation), profiles, groups
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().run {
            conversation = getParcelable(ArgConversation)
            messages = getParcelableArrayList(ArgMessages) ?: emptyList()

            profiles = getSerializable(ArgProfiles) as? HashMap<Int, VkUser> ?: hashMapOf()
            groups = getSerializable(ArgGroups) as? HashMap<Int, VkGroup> ?: hashMapOf()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        fillRecyclerView()
    }

    private fun fillRecyclerView() {
        adapter.setItems(messages)
        binding.recyclerView.adapter = adapter
    }

    fun scrollToMessage(messageId: Int) {
        adapter.searchMessageIndex(messageId)?.let { index ->
            binding.recyclerView.scrollToPosition(index)
        }
    }

    fun openForwardsScreen(
        conversation: VkConversation,
        messages: List<VkMessage>,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ) {
        requireActivityRouter().navigateTo(
            Screens.ForwardedMessages(conversation, messages, profiles, groups)
        )
    }

}