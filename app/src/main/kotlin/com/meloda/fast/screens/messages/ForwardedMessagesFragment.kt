package com.meloda.fast.screens.messages

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentForwardedMessagesBinding
import com.meloda.fast.ext.getParcelableArrayListCompat
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.getSerializableCompat

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

    private val binding by viewBinding(FragmentForwardedMessagesBinding::bind)

    private var conversation: VkConversation? = null
    private var messages: List<VkMessage> = emptyList()
    private var profiles = UsersIdsList.Empty
    private var groups = GroupsIdsList.Empty

    private val adapter: MessagesHistoryAdapter by lazy {
        MessagesHistoryAdapter(
            this, requireNotNull(conversation), profiles, groups
        )
    }

    open class IdsMap<T> : HashMap<Int, T>() {
        val ids get() = keys
    }

    class MessagesIdsList : IdsMap<VkMessage>() {
        companion object {
            val Empty get() = MessagesIdsList()
        }
    }

    class UsersIdsList : IdsMap<VkUser>() {
        companion object {
            val Empty get() = UsersIdsList()
        }
    }

    class GroupsIdsList : IdsMap<VkGroup>() {
        companion object {
            val Empty get() = GroupsIdsList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().run {
            conversation = getParcelableCompat(ArgConversation, VkConversation::class.java)

            messages = getParcelableArrayListCompat(ArgMessages, VkMessage::class.java)
                ?: emptyList()

            profiles =
                getSerializableCompat(ArgProfiles, UsersIdsList::class.java) ?: UsersIdsList.Empty
            groups =
                getSerializableCompat(ArgGroups, GroupsIdsList::class.java) ?: GroupsIdsList.Empty
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