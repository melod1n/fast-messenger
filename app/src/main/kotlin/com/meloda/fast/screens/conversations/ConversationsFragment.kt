package com.meloda.fast.screens.conversations

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.appbar.AppBarLayout
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class ConversationsFragment :
    BaseViewModelFragment<ConversationsViewModel>(R.layout.fragment_conversations) {

    companion object {
        const val TAG = "ConversationsFragment"
    }

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

    private var isPaused = false
    private var isExpanded = true

    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareViews()

        binding.recyclerView.adapter = adapter

        binding.createChat.setOnClickListener {}

        UserConfig.vkUser.observe(viewLifecycleOwner) {
            it?.let { user -> binding.avatar.load(user.photo200) { crossfade(100) } }
        }

        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (isPaused) return@OnOffsetChangedListener

            if (verticalOffset <= -100) {
                binding.avatarContainer.alpha = 0f
                return@OnOffsetChangedListener
            }

            val alpha = 1 - abs(verticalOffset * 0.01).toFloat()

            binding.avatarContainer.alpha = alpha
        })

        if (isPaused) {
            isPaused = false
            return
        }

        binding.toolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.test)

        viewModel.loadProfileUser()
        viewModel.loadConversations()
    }

    override fun onEvent(event: VKEvent) {
        super.onEvent(event)
        when (event) {
            is ConversationsLoaded -> refreshConversations(event)
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
            setOnRefreshListener { viewModel.loadConversations() }
        }
    }

    private fun refreshConversations(event: ConversationsLoaded) {
        adapter.profiles += event.profiles
        adapter.groups += event.groups

        fillRecyclerView(event.conversations)
    }

    private fun fillRecyclerView(values: List<VkConversation>) {
        adapter.values.clear()
        adapter.values += values
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    private fun onItemClick(position: Int) {
        val conversation = adapter[position]
        val user = if (conversation.isUser()) adapter.profiles[conversation.id] else null
        val group = if (conversation.isGroup()) adapter.groups[conversation.id] else null

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
        binding.createChat.performClick()
        return true
    }

}