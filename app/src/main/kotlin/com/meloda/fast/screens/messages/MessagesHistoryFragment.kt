package com.meloda.fast.screens.messages

import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import com.meloda.fast.databinding.FragmentMessagesHistoryBinding
import com.meloda.fast.extensions.TextViewExtensions.clear
import com.meloda.fast.extensions.isNotVisible
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

@AndroidEntryPoint
class MessagesHistoryFragment :
    BaseViewModelFragment<MessagesHistoryViewModel>(R.layout.fragment_messages_history) {

    override val viewModel: MessagesHistoryViewModel by viewModels()
    private val binding: FragmentMessagesHistoryBinding by viewBinding()

    private val action = MutableLiveData<Action>()

    private enum class Action {
        RECORD, SEND
    }

    private val user: VkUser? by lazy {
        requireArguments().getParcelable("user")
    }

    private val group: VkGroup? by lazy {
        requireArguments().getParcelable("group")
    }

    private val conversation: VkConversation by lazy {
        requireNotNull(requireArguments().getParcelable("conversation"))
    }

    private val adapter: MessagesHistoryAdapter by lazy {
        MessagesHistoryAdapter(requireContext(), mutableListOf(), conversation).also {
            it.onItemClickListener = this::onItemClick
            it.itemLongClickListener = this::onItemLongClick
        }
    }

    private val replyMessage = MutableLiveData<VkMessage?>()
    private val isAttachmentPanelVisible = MutableLiveData(false)

    private var timestampTimer: Timer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = when {
            conversation.isChat() -> conversation.title
            conversation.isUser() -> user?.toString()
            conversation.isGroup() -> group?.name
            else -> null
        }

        binding.title.ellipsize = TextUtils.TruncateAt.END
        binding.status.ellipsize = TextUtils.TruncateAt.END

        binding.title.text = title ?: "..."

        val status = when {
            conversation.isChat() -> "${conversation.membersCount} members"
            conversation.isUser() -> when {
                // TODO: 9/15/2021 user normal time
                user?.online == true -> "Online"
                user?.lastSeen != null -> "Last seen at ${
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(user?.lastSeen!! * 1000L)
                }"
                else -> if (user?.lastSeenStatus != null) "Last seen ${user?.lastSeenStatus!!}" else "Last seen recently"
            }
            conversation.isGroup() -> if (group?.membersCount != null) "${group?.membersCount} members" else "Group"
            else -> null
        }

        binding.status.text = status ?: "..."

        prepareAvatar()

        prepareViews()

        binding.recyclerView.adapter = adapter

        viewModel.loadHistory(conversation.id)

        binding.action.setOnClickListener { performAction() }

        binding.recyclerView.addOnLayoutChangeListener { _, i, i2, i3, bottom, i5, i6, i7, oldBottom ->
            if (bottom >= oldBottom) return@addOnLayoutChangeListener
            val lastVisiblePosition =
                (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            if (lastVisiblePosition <= adapter.lastPosition - 10) return@addOnLayoutChangeListener

            binding.recyclerView.postDelayed({
                binding.recyclerView.scrollToPosition(adapter.lastPosition)
            }, 25)
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val firstPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                val message = adapter.getOrNull(firstPosition)
                message?.let {
                    binding.timestamp.isVisible = true

                    val time = "${
                        TimeUtils.getLocalizedDate(
                            requireContext(),
                            it.date * 1000L
                        )
                    }, ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(it.date * 1000L)}"

                    binding.timestamp.text = time

                    if (timestampTimer != null) {
                        timestampTimer?.cancel()
                        timestampTimer = null
                    }

                    timestampTimer = Timer()
                    timestampTimer?.schedule(2500) {
                        recyclerView.post { binding.timestamp.isVisible = false }
                    }
                }

                super.onScrolled(recyclerView, dx, dy)
            }
        })

        binding.message.doAfterTextChanged {
            val canSend =
                it.toString().isNotBlank()

            val newValue =
                if (canSend) Action.SEND
                else Action.RECORD

            if (action.value != newValue) action.value = newValue
        }

        action.observe(viewLifecycleOwner) {
            binding.action.animate()
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(100)
                .withEndAction {
                    binding.action.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }.start()

            when (it) {
                Action.RECORD -> {
                    binding.action.setImageResource(R.drawable.ic_round_mic_24)
                }
                Action.SEND -> {
                    binding.action.setImageResource(R.drawable.ic_round_send_24)
                }
                else -> return@observe
            }
        }

        isAttachmentPanelVisible.observe(viewLifecycleOwner) {
            val layoutParams = binding.refreshLayout.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.bottomMargin =
                if (it) (binding.attachmentPanel.height / 1.5).roundToInt() else 0
        }

        hideAttachmentPanel(duration = 1)

        binding.avatar.setOnClickListener {
            val isShown = binding.attachmentPanel.isVisible

            if (isShown) {
                hideAttachmentPanel()
            } else {
                showAttachmentPanel()
            }
        }

        binding.attachmentPanel.setOnClickListener c@{
            val message = replyMessage.value ?: return@c

            val index = adapter.values.indexOf(message)
            if (index == -1) return@c

            binding.recyclerView.smoothScrollToPosition(index)
        }

        binding.dismissReply.setOnClickListener {
            if (replyMessage.value != null) replyMessage.value = null

            hideAttachmentPanel()
        }
    }

    private fun prepareAvatar() {
        val avatar = when {
            conversation.ownerId == VKConstants.FAST_GROUP_ID -> null
            conversation.isUser() -> user?.photo200
            conversation.isGroup() -> group?.photo200
            conversation.isChat() -> conversation.photo200
            else -> null
        }

        binding.avatar.isVisible = avatar != null

        if (avatar == null) {
            binding.avatarPlaceholder.isVisible = true

            if (conversation.ownerId == VKConstants.FAST_GROUP_ID) {
                binding.placeholderBack.setImageDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(requireContext(), R.color.a1_400)
                    )
                )
                binding.placeholder.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.a1_0))
                binding.placeholder.setImageResource(R.drawable.ic_fast_logo)
                binding.placeholder.setPadding(18)
            } else {
                binding.placeholderBack.setImageDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(requireContext(), R.color.n1_50)
                    )
                )
                binding.placeholder.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.n2_500))
                binding.placeholder.setImageResource(R.drawable.ic_account_circle_cut)
                binding.placeholder.setPadding(0)
                binding.avatar.setImageDrawable(null)
            }
        } else {
            binding.avatar.load(avatar) {
                crossfade(200)
                target {
                    binding.avatarPlaceholder.isVisible = false
                    binding.avatar.setImageDrawable(it)
                }
            }
        }

        binding.phantomIcon.isVisible = conversation.isPhantom
        binding.online.isVisible = user?.online == true
        binding.pin.isVisible = conversation.isPinned
    }

    private fun showAttachmentPanel(duration: Long = 250) {
        if (isAttachmentPanelVisible.value == false) isAttachmentPanelVisible.value = true

        binding.attachmentPanel.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(duration)
            .withStartAction { binding.attachmentPanel.isVisible = true }
            .start()
    }

    private fun hideAttachmentPanel(duration: Long = 250) {
        if (isAttachmentPanelVisible.value == true) isAttachmentPanelVisible.value = false

        binding.attachmentPanel.animate()
            .alpha(0f)
            .translationY(50f)
            .setDuration(duration)
            .withEndAction { binding.attachmentPanel.isVisible = false }
            .start()
    }

    private fun performAction() {
        if (action.value == Action.RECORD) {
            return
        } else if (action.value == Action.SEND) {
            val messageText = binding.message.text.toString().trim()
            if (messageText.isBlank()) return

            val date = System.currentTimeMillis()

            var message = VkMessage(
                id = -1,
                text = messageText,
                isOut = true,
                peerId = conversation.id,
                fromId = UserConfig.userId,
                date = (date / 1000).toInt(),
                randomId = 0,
                replyMessage = replyMessage.value
            )

            adapter.add(message)
            adapter.notifyItemInserted(adapter.actualSize - 1)
            binding.recyclerView.smoothScrollToPosition(adapter.lastPosition)
            binding.message.clear()

            val replyMessage = replyMessage.value

            this.replyMessage.value = null
            hideAttachmentPanel()

            viewModel.sendMessage(
                peerId = conversation.id,
                message = messageText,
                randomId = 0,
                replyTo = replyMessage?.id
            ) { message = message.copyMessage(id = it) }
        }
    }

    override fun onEvent(event: VKEvent) {
        super.onEvent(event)

        when (event) {
            is MessagesMarkAsImportant -> markMessagesAsImportant(event)
            is MessagesLoaded -> refreshMessages(event)
            is StartProgressEvent -> onProgressStarted()
            is StopProgressEvent -> onProgressStopped()
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
            setOnRefreshListener { viewModel.loadHistory(peerId = conversation.id) }
        }
    }

    private fun markMessagesAsImportant(event: MessagesMarkAsImportant) {
        var changed = false
        val positions = mutableListOf<Int>()

        for (i in adapter.values.indices) {
            val message = adapter.values[i]
            if (event.messagesIds.contains(message.id)) {
                if (!changed) changed = true

                positions.add(i)

                adapter.values[i] = message.copyMessage(
                    important = event.important
                )
            }
        }

        if (changed) positions.forEach { adapter.notifyItemChanged(it) }
    }

    private fun refreshMessages(event: MessagesLoaded) {
        adapter.profiles += event.profiles
        adapter.groups += event.groups

        fillRecyclerView(event.messages)
    }

    private fun fillRecyclerView(values: List<VkMessage>) {
        val smoothScroll = adapter.isNotEmpty()

        adapter.values.clear()
        adapter.values += values.sortedBy { it.date }
        adapter.notifyItemRangeChanged(0, adapter.itemCount)

        if (smoothScroll) binding.recyclerView.smoothScrollToPosition(adapter.lastPosition)
        else binding.recyclerView.scrollToPosition(adapter.lastPosition)
    }

    private fun onItemClick(position: Int, view: View) {
        val message = adapter.values[position]
        if (message.action != null) return

//        val popupMenu = PopupMenu(requireContext(), view)
//
//        val reply = popupMenu.menu.add(
//            getString(R.string.message_context_action_reply)
//        )
//
//        reply.icon =
//            ContextCompat.getDrawable(
//                requireContext(),
//                R.drawable.ic_attachment_wall_reply
//            )?.constantState?.newDrawable()?.also {
//                it.setTint(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.textColorSecondaryVariant
//                    )
//                )
//            }
//
//        val important = popupMenu.menu.add(
//            getString(
//                if (message.important) R.string.message_context_action_unmark_as_important
//                else R.string.message_context_action_mark_as_important
//            )
//        )
//
//        important.icon =
//            ContextCompat.getDrawable(
//                requireContext(),
//                R.drawable.ic_star_border
//            )?.constantState?.newDrawable()?.also {
//                it.setTint(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        R.color.textColorSecondaryVariant
//                    )
//                )
//            }
//
//        popupMenu.setForceShowIcon(true)
//        popupMenu.setOnMenuItemClickListener {
//            when (it) {
//                reply -> {
//                    val title = when {
//                        message.isGroup() && message.group.value != null -> message.group.value?.name
//                        message.isUser() && message.user.value != null -> message.user.value?.fullName
//                        else -> null
//                    }
//
//                    if (replyMessage.value != message) replyMessage.value = message
//
//                    binding.replyMessageTitle.text = title
//                    binding.replyMessageText.text = message.text ?: "[no_message]"
//
//                    if (binding.attachmentPanel.isNotVisible) binding.avatar.performClick()
//                    true
//                }
//
//                important -> {
//                    viewModel.markAsImportant(
//                        messagesIds = listOf(message.id),
//                        important = !message.important
//                    )
//                    true
//                }
//
//                else -> false
//            }
//        }
//        popupMenu.show()

        val reply = getString(R.string.message_context_action_reply)

        val important = getString(
            if (message.important) R.string.message_context_action_unmark_as_important
            else R.string.message_context_action_mark_as_important
        )

        val params = arrayOf(reply, important)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setItems(params) { _, which ->
                when (params[which]) {
                    important -> viewModel.markAsImportant(
                        messagesIds = listOf(message.id),
                        important = !message.important
                    )
                    reply -> {
                        val title = when {
                            message.isGroup() && message.group.value != null -> message.group.value?.name
                            message.isUser() && message.user.value != null -> message.user.value?.fullName
                            else -> null
                        }

                        if (replyMessage.value != message) replyMessage.value = message

                        binding.replyMessageTitle.text = title
                        binding.replyMessageText.text = message.text ?: "[no_message]"

                        if (binding.attachmentPanel.isNotVisible) binding.avatar.performClick()
                    }
                }
            }

        dialog.show()

    }

    private fun onItemLongClick(position: Int): Boolean {

        return true
    }

}