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
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.databinding.FragmentMessagesHistoryBinding
import com.meloda.fast.extensions.TextViewExtensions.clear
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
        RECORD, SEND, EDIT
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

    private var timestampTimer: Timer? = null

    private lateinit var attachmentController: AttachmentPanelController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachmentController = AttachmentPanelController().init()

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
                when {
                    attachmentController.isEditing -> Action.EDIT
                    canSend -> Action.SEND
                    else -> Action.RECORD
                }

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
                Action.EDIT -> {
                    binding.action.setImageResource(R.drawable.ic_round_done_24)
                }
                else -> return@observe
            }
        }

        attachmentController.isPanelVisible.observe(viewLifecycleOwner) {
            val layoutParams = binding.refreshLayout.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.bottomMargin =
                if (it) (binding.attachmentPanel.height / 1.5).roundToInt() else 0
        }

        binding.attachmentPanel.setOnClickListener c@{
            val message = attachmentController.message.value ?: return@c

            val index = adapter.values.indexOf(message)
            if (index == -1) return@c

            binding.recyclerView.smoothScrollToPosition(index)
        }

        binding.dismissReply.setOnClickListener {
            if (attachmentController.message.value != null)
                attachmentController.message.value = null
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
                replyMessage = attachmentController.message.value
            )

            adapter.add(message)
            adapter.notifyItemInserted(adapter.actualSize - 1)
            binding.recyclerView.smoothScrollToPosition(adapter.lastPosition)
            binding.message.clear()

            val replyMessage = attachmentController.message.value
            attachmentController.message.value = null

            viewModel.sendMessage(
                peerId = conversation.id,
                message = messageText,
                randomId = 0,
                replyTo = replyMessage?.id
            ) { message = message.copyMessage(id = it) }
        }
    }

    override fun onEvent(event: VkEvent) {
        super.onEvent(event)

        when (event) {
            is MessagesMarkAsImportant -> markMessagesAsImportant(event)
            is MessagesLoaded -> refreshMessages(event)
            is MessagesPin -> conversation.pinnedMessage = event.message
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

        val time = getString(
            R.string.time_format,
            SimpleDateFormat(
                "dd.MM.yyyy, HH:mm:ss",
                Locale.getDefault()
            ).format(message.date * 1000L)
        )

        val reply = getString(R.string.message_context_action_reply)

        val isMessageAlreadyPinned = message.id == conversation.pinnedMessage?.id

        val pin = getString(
            if (isMessageAlreadyPinned) R.string.message_context_action_unpin
            else R.string.message_context_action_pin
        )

        val edit = getString(R.string.message_context_action_edit)

        val important = getString(
            if (message.important) R.string.message_context_action_unmark_as_important
            else R.string.message_context_action_mark_as_important
        )

        val params = mutableListOf<String>()
        params.add(reply)

        if (conversation.canChangePin) {
            params.add(pin)
        }

        if (message.canEdit()) {
            params.add(edit)
        }

        params.add(important)

        val arrayParams = params.toTypedArray()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(time)
            .setItems(arrayParams) { _, which ->
                when (params[which]) {
                    important -> viewModel.markAsImportant(
                        messagesIds = listOf(message.id),
                        important = !message.important
                    )
                    reply -> {
                        if (attachmentController.message.value != message)
                            attachmentController.message.value = message
                    }
                    pin -> viewModel.pinMessage(
                        peerId = conversation.id,
                        messageId = message.id,
                        pin = !isMessageAlreadyPinned
                    )
                    edit -> {
                        attachmentController.isEditing = true

                        if (attachmentController.message.value != message)
                            attachmentController.message.value = message
                    }
                }
            }

        dialog.show()

    }

    private fun onItemLongClick(position: Int): Boolean {

        return true
    }

    private inner class AttachmentPanelController {
        val isPanelVisible = MutableLiveData(false)
        val message = MutableLiveData<VkMessage?>()

        var isEditing = false

        fun init(): AttachmentPanelController {
            message.observe(viewLifecycleOwner) { value ->
                if (value != null) {
                    applyMessage(value)
                } else {
                    clearMessage()
                }
            }

            message.value = null
            return this
        }

        private fun applyMessage(message: VkMessage) {
            showPanel()

            val title = when {
                message.isGroup() && message.group.value != null -> message.group.value?.name
                message.isUser() && message.user.value != null -> message.user.value?.fullName
                else -> null
            }

            binding.replyMessageTitle.text = title
            binding.replyMessageText.text = message.text ?: "[no_message]"

            if (isEditing) {
                binding.message.setText(message.text ?: "[no_message]")
            }
        }

        private fun clearMessage() {
            hidePanel()

            binding.replyMessageTitle.clear()
            binding.replyMessageText.clear()

            if (isEditing) {
                isEditing = false
                binding.message.clear()
            }
        }

        private fun showPanel(duration: Long = 250) {
            if (attachmentController.isPanelVisible.value == false)
                attachmentController.isPanelVisible.value = true

            binding.attachmentPanel.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .withStartAction { binding.attachmentPanel.isVisible = true }
                .start()
        }

        private fun hidePanel(duration: Long = 250) {
            if (attachmentController.isPanelVisible.value == true)
                attachmentController.isPanelVisible.value = false

            binding.attachmentPanel.animate()
                .alpha(0f)
                .translationY(50f)
                .setDuration(duration)
                .withEndAction { binding.attachmentPanel.isVisible = false }
                .start()
        }

    }

}