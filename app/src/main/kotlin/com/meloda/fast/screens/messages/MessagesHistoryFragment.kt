package com.meloda.fast.screens.messages

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
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
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.TimeUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

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
            it.itemClickListener = this::onItemClick
            it.itemLongClickListener = this::onItemLongClick
        }
    }

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

        val avatar = when {
            conversation.isChat() -> conversation.photo200
            conversation.isUser() -> user?.photo200
            conversation.isGroup() -> group?.photo200
            else -> null
        }

        binding.avatar.load(avatar) {
            crossfade(false)
            error(ColorDrawable(Color.RED))
        }

        binding.online.isVisible = user?.online == true

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
            val newValue = if (it.toString().isNotBlank()) Action.SEND
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
    }


    private fun performAction() {
        if (action.value == Action.RECORD) {

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
                randomId = 0
            )

            adapter.add(message)
            adapter.notifyDataSetChanged()
            binding.recyclerView.smoothScrollToPosition(adapter.lastPosition)
            binding.message.clear()

            viewModel.sendMessage(
                peerId = conversation.id,
                message = messageText,
                randomId = 0
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
        for (i in adapter.values.indices) {
            val message = adapter.values[i]
            if (event.messagesIds.contains(message.id)) {
                if (!changed) changed = true
                adapter.values[i] = message.copyMessage(
                    important = event.important
                )
            }
        }

        if (changed) adapter.notifyDataSetChanged()
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

    private fun onItemClick(position: Int) {
        val message = adapter.values[position]
        if (message.action != null) return

        val important = if (message.important) "Unmark as important" else "Mark as important"

        val params = arrayOf(important)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setItems(params) { _, which ->
                if (which == 0) {
                    viewModel.markAsImportant(
                        messagesIds = listOf(message.id),
                        important = !message.important
                    )
                }
            }

        dialog.show()

    }

    private fun onItemLongClick(position: Int): Boolean {

        return true
    }

}