package com.meloda.fast.screens.messages

import android.os.Bundle
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.meloda.fast.R
import com.meloda.fast.base.BaseVMFragment
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class ConversationsFragment : BaseVMFragment<ConversationsVM>(R.layout.fragment_conversations) {

    override val viewModel: ConversationsVM by viewModels()
    private val binding: FragmentConversationsBinding by viewBinding()

    private lateinit var adapter: ConversationsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareViews()

        viewModel.loadConversations()
    }

    override fun onEvent(event: VKEvent) {
        super.onEvent(event)
        when (event) {
            StartProgressEvent -> onProgressStarted()
            StopProgressEvent -> onProgressStopped()
        }
    }

    private fun onProgressStarted() {
        if (adapter.isEmpty())
        binding.progressBar.isVisible = true
    }

    private fun onProgressStopped() {
        binding.progressBar.isVisible = false
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
            setOnRefreshListener { }
        }
    }

}