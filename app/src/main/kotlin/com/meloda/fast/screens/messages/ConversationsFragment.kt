package com.meloda.fast.screens.messages

import android.os.Bundle
import android.util.Log
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meloda.fast.R
import com.meloda.fast.api.LoadManager
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.base.BaseViewModelFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class ConversationsFragment :
    BaseViewModelFragment<ConversationsViewModel>(R.layout.fragment_conversations) {

    companion object {
        val TAG: String = ConversationsFragment::class.java.name
    }

//    @Inject
//    lateinit var loadManager: LoadManager

    override val viewModel: ConversationsViewModel by viewModels()
    private val binding: FragmentConversationsBinding by viewBinding()

    private lateinit var adapter: ConversationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareViews()

        adapter = ConversationsAdapter(requireContext(), mutableListOf())
        binding.recyclerView.adapter = adapter

        viewModel.loadConversations()
    }

    override fun onEvent(event: VKEvent) {
        super.onEvent(event)
        when (event) {
            is ConversationsLoaded -> prepareData(event)
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

    }

    private fun prepareRefreshLayout() {
        with(binding.refreshLayout) {
            setProgressViewOffset(
                true,
                AndroidUtils.px(40).roundToInt(),
                AndroidUtils.px(96).roundToInt()
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

    private fun prepareData(event: ConversationsLoaded) {
        val conversations = mutableListOf<VkConversation>()

        val timeInMillis = measureTimeMillis {
            for (i in event.conversations.indices) {
                val baseConversation = event.conversations[i]
                val baseMessage = event.messages[i]

                conversations += VkConversation(
                    id = baseConversation.peer.id,
                    title = baseConversation.chatSettings?.title,
                    lastMessage = VkMessage(
                        id = baseMessage.id,
                        text = baseMessage.text,
                        isOut = baseMessage.out == 1,
                        peerId = baseMessage.peerId,
                        fromId = baseMessage.fromId,
                        date = baseMessage.date
                    )
                )
            }
        }

        Log.d(TAG, "prepareData: $timeInMillis ms")

        fillRecyclerView(conversations)

        lifecycleScope.launch {
            LoadManager.users.load(listOf(1, 2, 3))
        }
    }

    private fun fillRecyclerView(values: List<VkConversation>) {
        adapter.values.clear()
        adapter.values += values
        adapter.notifyDataSetChanged()
    }

}