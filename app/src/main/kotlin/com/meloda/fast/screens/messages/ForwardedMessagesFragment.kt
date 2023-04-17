package com.meloda.fast.screens.messages

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.terrakok.cicerone.Router
import com.meloda.fast.R
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentForwardedMessagesBinding
import com.meloda.fast.ext.getParcelableArrayListCompat
import com.meloda.fast.ext.getParcelableCompat
import com.meloda.fast.ext.getSerializableCompat
import dev.chrisbanes.insetter.applyInsetter
import org.koin.android.ext.android.inject

class ForwardedMessagesFragment : BaseFragment(R.layout.fragment_forwarded_messages) {

    private val router: Router by inject()

    private val binding by viewBinding(FragmentForwardedMessagesBinding::bind)

    private var conversation: VkConversationDomain? = null
    private var messages: List<VkMessage> = emptyList()
    private var profiles = hashMapOf<Int, VkUser>()
    private var groups = hashMapOf<Int, VkGroup>()

    private val adapter: MessagesHistoryAdapter by lazy {
        MessagesHistoryAdapter(
            this, requireNotNull(conversation), profiles, groups
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireArguments().run {
            conversation = getParcelableCompat(ArgConversation, VkConversationDomain::class.java)

            messages = getParcelableArrayListCompat(ArgMessages, VkMessage::class.java)
                ?: emptyList()

            profiles =
                getSerializableCompat(ArgProfiles, HashMap::class.java) as? HashMap<Int, VkUser>
                    ?: hashMapOf()
            groups =
                getSerializableCompat(ArgGroups, HashMap::class.java) as? HashMap<Int, VkGroup>
                    ?: hashMapOf()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.applyInsetter {
            type(navigationBars = true) { padding() }
        }

        binding.toolbar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

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
        conversation: VkConversationDomain,
        messages: List<VkMessage>,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ) {
        router.navigateTo(
            Screens.ForwardedMessages(conversation, messages, profiles, groups)
        )
    }

    companion object {
        private const val ArgConversation = "conversation"
        private const val ArgMessages = "messages"
        private const val ArgProfiles = "profiles"
        private const val ArgGroups = "groups"

        fun newInstance(
            conversation: VkConversationDomain,
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
}
