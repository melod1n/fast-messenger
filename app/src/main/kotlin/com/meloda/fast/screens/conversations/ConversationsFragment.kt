package com.meloda.fast.screens.conversations

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.appbar.AppBarLayout
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
import com.meloda.fast.common.AppSettings
import com.meloda.fast.common.dataStore
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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
    private var isExpanded = true

    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareViews()

        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            requireContext().dataStore.data.map {
                adapter.isMultilineEnabled = it[AppSettings.keyIsMultilineEnabled] ?: true
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
            }.collect { }
        }

        binding.createChat.setOnClickListener {}

        UserConfig.vkUser.observe(viewLifecycleOwner) {
            it?.let { user -> binding.avatar.load(user.photo200) { crossfade(100) } }
        }

        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            if (isPaused) return@OnOffsetChangedListener


//            if (verticalOffset <= -100) {
//                binding.avatarContainer.alpha = 0f
//                return@OnOffsetChangedListener
//            }

            // from 0 to -294
            // from 0 to 29

            // if -147
            // 30 - value

            var value = 30 - (abs(verticalOffset) * 0.1).roundToInt()

            val bottomPadding = 0
//                if (verticalOffset > -150) AndroidUtils.px(30).roundToInt()
//                else (30 + abs(verticalOffset) * 0.1).roundToInt()

            val endPadding = 0
//                if (verticalOffset > 30) 30
//                else (abs(verticalOffset) * 0.1).roundToInt()

            binding.avatarContainer.updatePadding(
                bottom = value,
                right = endPadding
            )


            println("Fast::ConversationsFragment::onOffset verticalOffset = $verticalOffset; bottomPadding = $value; endPadding = $endPadding")


//            binding.avatarContainer.alpha = alpha
        })

        binding.toolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.test)


        binding.avatar.setOnClickListener {
            avatarPopupMenu.show()
        }

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

    private fun showLogOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.log_out_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                UserConfig.clear()
                AppGlobal.appDatabase.clearAllTables()
                requireActivity().finishAffinity()
                requireActivity().startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onEvent(event: VkEvent) {
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