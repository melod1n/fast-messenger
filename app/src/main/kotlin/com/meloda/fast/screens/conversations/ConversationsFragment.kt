package com.meloda.fast.screens.conversations

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.viewbinding.library.fragment.viewBinding
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.activity.RootActivity
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
import com.meloda.fast.extensions.dpToPx
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
            mutableListOf(),
            hashMapOf(),
            hashMapOf()
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

    private var isPaused = false

    private var actualAvatarContainerPadding: Int = 0
    private var isScrollingBottom: Boolean = false

    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareViews()

        actualAvatarContainerPadding = binding.avatarContainer.paddingBottom

        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            requireContext().dataStore.data.map {
                adapter.isMultilineEnabled = it[AppSettings.keyIsMultilineEnabled] ?: true
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }.collect()
        }

        binding.createChat.setOnClickListener {}

        UserConfig.vkUser.observe(viewLifecycleOwner) {
            it?.let { user -> binding.avatar.load(user.photo200) { crossfade(100) } }
        }

//        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
//            if (isPaused) return@OnOffsetChangedListener
//
//            animateAppBarPaddings(verticalOffset <= -100)
//        })

        applyScrollListener()

        binding.avatar.setOnClickListener { avatarPopupMenu.show() }

        binding.avatar.setOnLongClickListener {
            lifecycleScope.launch {
                requireContext().dataStore.edit { settings ->
                    val isMultilineEnabled = settings[AppSettings.keyIsMultilineEnabled] ?: true
                    settings[AppSettings.keyIsMultilineEnabled] = !isMultilineEnabled

                    adapter.isMultilineEnabled = !isMultilineEnabled
                    adapter.notifyItemRangeChanged(0, adapter.itemCount)
                }
            }
            true
        }

        if (isPaused) {
            isPaused = false
            return
        }

        viewModel.loadProfileUser()
        viewModel.loadConversations()
    }

    private fun applyScrollListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val firstPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

                val scrollToBottom = dy > 0

                if (!scrollToBottom && firstPosition > 0) return

                if (isScrollingBottom != scrollToBottom) {
                    isScrollingBottom = scrollToBottom

                    animateAppBarPaddings(scrollToBottom)
                }
            }
        })
    }

    private var paddingAnimator: ValueAnimator? = null

    private fun animateAppBarPaddings(scrollToBottom: Boolean, overrideStartPadding: Int? = null) {
        val startPadding = overrideStartPadding ?: binding.avatarContainer.paddingBottom
        val endPadding = (if (scrollToBottom) 10 else 30).dpToPx()

        if (paddingAnimator?.isRunning == true) {
            paddingAnimator?.pause()
            paddingAnimator = null

            animateAppBarPaddings(scrollToBottom, actualAvatarContainerPadding)
            return
        }

        paddingAnimator = ValueAnimator.ofInt(startPadding, endPadding).apply {
            duration = 150
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener {
                val padding = it.animatedValue as Int

                this@ConversationsFragment.actualAvatarContainerPadding = padding

                binding.avatarContainer.updatePadding(
                    bottom = padding,
                    right = padding
                )
            }
        }
        paddingAnimator?.start()
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

                    requireActivity().finishAffinity()
                    requireActivity().startActivity(
                        Intent(
                            requireContext(),
                            RootActivity::class.java
                        )
                    )
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

            is MessageNewEvent -> onMessageNew(event)
            is MessageEditEvent -> onMessageEdit(event)
        }
    }

    private fun onProgressStarted() {
        binding.progressBar.isVisible = adapter.isEmpty()
        binding.refreshLayout.isRefreshing = adapter.isNotEmpty()
    }

    private fun onProgressStopped() {
        binding.progressBar.isVisible = false
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

        fillRecyclerView(event.conversations)

        val pinnedConversations = event.conversations.filter { it.isPinned }
        adapter.pinnedCount = pinnedConversations.count()
    }

    private fun fillRecyclerView(values: List<VkConversation>) {
        adapter.values.clear()
        adapter.values += values
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

        findNavController().navigate(
            R.id.toMessagesHistory,
            bundleOf(
                "conversation" to adapter[position],
                "user" to user,
                "group" to group
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
            val firstFiveDialogs = adapter.values.subList(0, 5)
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
        val index = adapter.removeConversation(conversationId) ?: return
        adapter.notifyItemRemoved(index)
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

    private fun onMessageNew(event: MessageNewEvent) {
        adapter.profiles += event.profiles
        adapter.groups += event.groups

        val message = event.message

        val conversationIndex = adapter.searchConversationIndex(message.peerId)
        if (conversationIndex == null) { // диалога нет в списке

        } else {
            val conversation = adapter[conversationIndex].also {
                it.lastMessage = message
                it.lastMessageId = message.id
                it.lastConversationMessageId = -1
            }

            if (conversation.isPinned) {
                adapter.notifyItemChanged(conversationIndex)
                return
            }

            val fromPosition = adapter.removeConversation(message.peerId) ?: return
            val toPosition = adapter.pinnedCount

            adapter.values.add(toPosition, conversation)
            adapter.submitList(adapter.values.toMutableList())
//            adapter.notifyItemMoved(fromPosition, 0)
//            binding.recyclerView.smoothScrollToPosition(0)
        }
    }

    private fun onMessageEdit(event: MessageEditEvent) {
        val message = event.message

        val conversationIndex = adapter.searchConversationIndex(message.peerId)
        if (conversationIndex == null) { // диалога нет в списке

        } else {
            adapter[conversationIndex].also {
                it.lastMessage = message
            }

            adapter.notifyItemChanged(conversationIndex)
        }
    }
}