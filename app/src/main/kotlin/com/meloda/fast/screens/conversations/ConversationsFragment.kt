package com.meloda.fast.screens.conversations

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.activity.MainActivity
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.addAvatarMenuItem
import com.meloda.fast.extensions.gone
import com.meloda.fast.extensions.tintMenuItemIcons
import com.meloda.fast.extensions.toggleVisibility
import com.meloda.fast.screens.settings.SettingsPrefsFragment
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversationsFragment :
    BaseViewModelFragment<ConversationsViewModel>(R.layout.fragment_conversations) {

    override val viewModel: ConversationsViewModel by viewModels()
    private val binding: FragmentConversationsBinding by viewBinding()

    private val adapter: ConversationsAdapter by lazy {
        ConversationsAdapter(
            requireContext(),
            ConversationsResourceProvider(requireContext())
        ).also {
            it.itemClickListener = this::onItemClick
            it.itemLongClickListener = this::onItemLongClick
        }
    }

    private val avatarPopupMenu: PopupMenu
        get() =
            PopupMenu(
                requireContext(),
                binding.toolbar,
                Gravity.BOTTOM or Gravity.END
            ).apply {
                menu.add("Settings")
                menu.add(getString(R.string.log_out))
                setOnMenuItemClickListener { item ->
                    return@setOnMenuItemClickListener when (item.title) {
                        getString(R.string.log_out) -> {
                            showLogOutDialog()
                            true
                        }
                        "Settings" -> {
                            requireActivityRouter().navigateTo(Screens.Settings())
                            true
                        }
                        else -> false
                    }
                }
            }

    private var toggle: ActionBarDrawerToggle? = null

    private val useNavDrawer: Boolean get() = (requireActivity() as MainActivity).useNavDrawer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.isMultilineEnabled =
            AppGlobal.preferences.getBoolean(SettingsPrefsFragment.PrefMultiline, true)

        prepareViews()

        binding.recyclerView.adapter = adapter

        binding.createChat.setOnClickListener {}

        binding.toolbar.tintMenuItemIcons(
            ContextCompat.getColor(
                requireContext(),
                R.color.colorPrimary
            )
        )

        binding.toolbar.menu[0].isVisible = false

        val avatarMenuItem = binding.toolbar.addAvatarMenuItem()
        syncAvatarMenuItem(avatarMenuItem)

        UserConfig.vkUser.observe(viewLifecycleOwner) { user ->
            user?.run {
                avatarMenuItem.actionView.findViewById<ImageView>(R.id.avatar)
                    .loadWithGlide(
                        url = this.photo200, crossFade = true, asCircle = true
                    )

                val header = (requireActivity() as MainActivity).binding.drawer.getHeaderView(0)
                header.findViewById<TextView>(R.id.name).text = user.fullName
                header.findViewById<ImageView>(R.id.avatar).loadWithGlide(
                    url = this.photo200, crossFade = true, asCircle = true
                )
            }
        }

        avatarMenuItem.actionView.run {
            setOnClickListener { avatarPopupMenu.show() }
        }

        viewModel.loadProfileUser()
        viewModel.loadConversations()

        syncToolbarToggle()

        binding.createChat.gone()

        setFragmentResultListener(SettingsPrefsFragment.KeyChangeMultiline) { _, bundle ->
            val enabled = bundle.getBoolean(SettingsPrefsFragment.ArgEnabled)

            if (adapter.isMultilineEnabled != enabled) {
                adapter.isMultilineEnabled = enabled
                adapter.refreshList()
            }
        }
    }

    private fun syncAvatarMenuItem(item: MenuItem) {
        item.isVisible = !useNavDrawer
    }

    private fun syncToolbarToggle() {
        (requireActivity() as MainActivity).let { activity ->
            if (useNavDrawer) {
                toggle = ActionBarDrawerToggle(
                    activity, activity.binding.drawerLayout,
                    binding.toolbar, R.string.app_name, R.string.app_name
                ).apply {
                    isDrawerSlideAnimationEnabled = false
                    activity.binding.drawerLayout.addDrawerListener(this)
                    syncState()
                }
            } else {
                toggle?.let { toggle ->
                    activity.binding.drawerLayout.removeDrawerListener(toggle)
                }
            }
        }
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

        viewModel.openMessagesHistoryScreen(conversation, user, group)
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