package com.meloda.fast.screens.conversations

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.AppSettings
import com.meloda.fast.common.dataStore
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.gone
import com.meloda.fast.extensions.toggleVisibility
import com.meloda.fast.screens.messages.MessagesHistoryFragment
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversationsFragment :
    BaseViewModelFragment<ConversationsViewModel>(R.layout.fragment_conversations) {

    override val viewModel: ConversationsViewModel by viewModels()
    private val binding: FragmentConversationsBinding by viewBinding()

    private val adapter: ConversationsAdapter by lazy {
        ConversationsAdapter(
            requireContext(),
            ConversationsResourceManager(requireContext())
        ).also {
            it.itemClickListener = this::onItemClick
            it.itemLongClickListener = this::onItemLongClick
        }
    }

    private val avatarPopupMenu: PopupMenu
        get() =
            PopupMenu(
                requireContext(),
                binding.avatar,
                Gravity.BOTTOM
            ).apply {
                menu.add(getString(R.string.log_out))
                setOnMenuItemClickListener { item ->
                    if (item.title == getString(R.string.log_out)) {
                        showLogOutDialog()
                        return@setOnMenuItemClickListener true
                    }

                    false
                }
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareViews()

        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            requireContext().dataStore.data.map {
                adapter.isMultilineEnabled = it[AppSettings.keyIsMultilineEnabled] ?: true
                adapter.refreshList()
            }.collect()
        }

        binding.createChat.setOnClickListener {}

        UserConfig.vkUser.observe(viewLifecycleOwner) { user ->
            user?.run { binding.avatar.loadWithGlide(url = this.photo200, crossFade = true) }
        }

        binding.avatar.setOnClickListener { avatarPopupMenu.show() }

        binding.avatar.setOnLongClickListener {
            lifecycleScope.launch {
                requireContext().dataStore.edit { settings ->
                    val isMultilineEnabled = settings[AppSettings.keyIsMultilineEnabled] ?: true
                    settings[AppSettings.keyIsMultilineEnabled] = !isMultilineEnabled

                    adapter.isMultilineEnabled = !isMultilineEnabled
                    adapter.refreshList()
                }
            }
            true
        }

        viewModel.loadProfileUser()
        viewModel.loadConversations()
    }

    private fun showLogOutDialog() {
        val isEasterEgg = UserConfig.userId == 37610580

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if (isEasterEgg) "Выйти внаружу?"
                else getString(R.string.sign_out_confirm_title)
            )
            .setMessage(R.string.sign_out_confirm)
            .setPositiveButton(
                if (isEasterEgg) "Выйти внаружу"
                else getString(R.string.action_sign_out)
            ) { _, _ ->
                lifecycleScope.launch(Dispatchers.Default) {
                    UserConfig.clear()
                    AppGlobal.appDatabase.clearAllTables()

                    viewModel.openRootScreen()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)
        when (event) {
            is StartProgressEvent -> onProgressStarted()
            is StopProgressEvent -> onProgressStopped()

            is ConversationsLoadedEvent -> refreshConversations(event)
            is ConversationsDeleteEvent -> deleteConversation(event.peerId)

            // TODO: 10-Oct-21 remove this and sort conversations list
            is ConversationsPinEvent -> {
                adapter.pinnedCount++
                viewModel.loadConversations()
            }
            is ConversationsUnpinEvent -> {
                adapter.pinnedCount--
                viewModel.loadConversations()
            }

            is MessagesNewEvent -> onMessageNew(event)
            is MessagesEditEvent -> onMessageEdit(event)
            is MessagesReadEvent -> onMessageRead(event)
        }
    }

    private fun onProgressStarted() {
        binding.progressBar.toggleVisibility(adapter.isEmpty())
        binding.refreshLayout.isRefreshing = adapter.isNotEmpty()
    }

    private fun onProgressStopped() {
        binding.progressBar.gone()
        binding.refreshLayout.isRefreshing = false
    }

    private fun prepareViews() {
        prepareRecyclerView()
        prepareRefreshLayout()
    }

    private fun prepareRecyclerView() {
        binding.recyclerView.itemAnimator = null
    }

    private fun prepareRefreshLayout() {
        with(binding.refreshLayout) {
            setProgressViewOffset(
                true, progressViewStartOffset, progressViewEndOffset
            )
            setProgressBackgroundColorSchemeColor(
                AndroidUtils.getThemeAttrColor(
                    requireContext(),
                    R.attr.colorSurface
                )
            )
            setColorSchemeColors(
                AndroidUtils.getThemeAttrColor(
                    requireContext(),
                    R.attr.colorAccent
                )
            )
            setOnRefreshListener { viewModel.loadConversations() }
        }
    }

    private fun refreshConversations(event: ConversationsLoadedEvent) {
        adapter.profiles += event.profiles
        adapter.groups += event.groups

        if (event.avatars != null) {
            event.avatars.forEach { avatar ->
                Glide.with(requireContext())
                    .load(avatar)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .preload(200, 200)
            }
        }

        val pinnedConversations = event.conversations.filter { it.isPinned }
        adapter.pinnedCount = pinnedConversations.count()

        fillRecyclerView(event.conversations)
    }

    private fun fillRecyclerView(values: List<VkConversation>) {
        adapter.submitList(values)
    }

    private fun onItemClick(position: Int) {
        val conversation = adapter[position]

        val user =
            if (conversation.isUser()) adapter.profiles[conversation.id]
            else null

        val group =
            if (conversation.isGroup()) adapter.groups[conversation.id]
            else null

        viewModel.openMessagesHistoryScreen(
            bundleOf(
                MessagesHistoryFragment.ARG_USER to user,
                MessagesHistoryFragment.ARG_GROUP to group,
                MessagesHistoryFragment.ARG_CONVERSATION to conversation
            )
        )
    }

    private fun onItemLongClick(position: Int): Boolean {
        showOptionsDialog(position)
        return true
    }

    private fun showOptionsDialog(position: Int) {
        val conversation = adapter[position]

        var canPinOneMoreDialog = true
        if (adapter.itemCount > 4) {
            val firstFiveDialogs = adapter.currentList.subList(0, 5)
            var pinnedCount = 0

            firstFiveDialogs.forEach { if (it.isPinned) pinnedCount++ }
            if (pinnedCount == 5 && position > 4) {
                canPinOneMoreDialog = false
            }
        }

        val pin = getString(
            if (conversation.isPinned) R.string.conversation_context_action_unpin
            else R.string.conversation_context_action_pin
        )

        val delete = getString(R.string.conversation_context_action_delete)

        val params = mutableListOf<String>()

        if (canPinOneMoreDialog) params += pin

        params += delete

        val arrayParams = params.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayParams) { _, which ->
                when (params[which]) {
                    pin -> showPinConversationDialog(conversation)
                    delete -> showDeleteConversationDialog(conversation.id)
                }
            }.show()
    }

    private fun showDeleteConversationDialog(conversationId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_delete_conversation)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteConversation(conversationId)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteConversation(conversationId: Int) {
        adapter.removeConversation(conversationId)
    }

    private fun showPinConversationDialog(conversation: VkConversation) {
        val isPinned = conversation.isPinned
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if (isPinned) R.string.confirm_unpin_conversation
                else R.string.confirm_pin_conversation
            )
            .setPositiveButton(
                if (isPinned) R.string.action_unpin
                else R.string.action_pin
            ) { _, _ ->
                viewModel.pinConversation(
                    peerId = conversation.id,
                    pin = !isPinned
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun onMessageNew(event: MessagesNewEvent) {
        adapter.profiles += event.profiles
        adapter.groups += event.groups

        val message = event.message

        val conversationIndex = adapter.searchConversationIndex(message.peerId)
        if (conversationIndex == null) { // диалога нет в списке

        } else {
            val conversation = adapter[conversationIndex]
            val newConversation = conversation.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastConversationMessageId = -1
            )
            if (!message.isOut) {
                newConversation.unreadCount += 1
            }

            if (conversation.isPinned) {
                adapter[conversationIndex] = newConversation
                return
            }

            val newList = adapter.cloneCurrentList()
            newList.removeAt(conversationIndex)

            val toPosition = adapter.pinnedCount
            newList.add(toPosition, newConversation)

            adapter.submitList(newList)
        }
    }

    private fun onMessageEdit(event: MessagesEditEvent) {
        val message = event.message

        val conversationIndex = adapter.searchConversationIndex(message.peerId)
        if (conversationIndex == null) { // диалога нет в списке

        } else {
            val conversation = adapter[conversationIndex]
            adapter[conversationIndex] = conversation.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastConversationMessageId = -1
            )
        }
    }

    private fun onMessageRead(event: MessagesReadEvent) {
        val conversationIndex = adapter.searchConversationIndex(event.peerId) ?: return

        val newConversation = adapter[conversationIndex].copy()

        if (event.isOut) {
            newConversation.outRead = event.messageId
        } else {
            newConversation.inRead = event.messageId
        }

        adapter[conversationIndex] = newConversation
    }
}