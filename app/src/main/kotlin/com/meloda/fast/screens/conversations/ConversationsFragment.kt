package com.meloda.fast.screens.conversations

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.adapter.AsyncDiffItemAdapter
import com.meloda.fast.base.viewmodel.BaseViewModelFragment
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.addAvatarMenuItem
import com.meloda.fast.ext.color
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.findIndex
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.tintMenuItemIcons
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.screens.conversations.adapter.conversationDelegate
import com.meloda.fast.screens.main.MainFragment
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConversationsFragment :
    BaseViewModelFragment<ConversationsViewModel>(R.layout.fragment_conversations) {

    override val viewModel: ConversationsViewModel by viewModels()
    private val binding by viewBinding(FragmentConversationsBinding::bind)

    private val adapter by lazy { AsyncDiffItemAdapter() }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareView()
        listenViewModel()

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
                    AppGlobal.cacheDatabase.clearAllTables()
                    setFragmentResult(
                        MainFragment.KeyStartServices,
                        bundleOf("enable" to false)
                    )

                    viewModel.openRootScreen()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun toggleProgress(isProgressing: Boolean) {
        view?.run {
            findViewById<View>(R.id.progress_bar).toggleVisibility(
                if (isProgressing) adapter.isEmpty() else false
            )
            findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing =
                if (isProgressing) adapter.isNotEmpty() else false
        }
    }

    private fun prepareView() {
        applyInsets()
        prepareAppBar()
        prepareToolbar()
        prepareCreateChat()
        prepareRecyclerView()
        prepareRefreshLayout()
    }

    private fun applyInsets() {
        binding.appBar.applyInsetter {
            type(statusBars = true) { padding() }
        }
        binding.recyclerView.applyInsetter {
            type(navigationBars = true) { padding() }
        }
    }

    private fun prepareAppBar() {
        binding.appBar.isLiftOnScroll = false
    }

    private fun prepareToolbar() {
        binding.toolbar.tintMenuItemIcons(color(R.color.colorPrimary))

        val avatarMenuItem = binding.toolbar.addAvatarMenuItem()

        UserConfig.vkUser.listenValue { user ->
            if (user == null) return@listenValue

            avatarMenuItem.actionView?.findViewById<ImageView>(R.id.avatar)
                ?.loadWithGlide {
                    imageUrl = user.photo200
                    crossFade = true
                    asCircle = true
                }
        }

        avatarMenuItem.actionView?.run {
            setOnClickListener { avatarPopupMenu.show() }
        }
    }

    private fun prepareCreateChat() {
        binding.createChat.setOnClickListener {}
        binding.createChat.gone()
    }

    private fun prepareRecyclerView() {
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private var scrollState = 0

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                scrollState = newState
            }


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val firstVisiblePosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()

                applyAppBarElevation(
                    scrollState != RecyclerView.SCROLL_STATE_IDLE && firstVisiblePosition > 0
                )
            }
        })

        val conversationsDelegate = conversationDelegate(
            onItemClickListener = { conversation ->
                val dataConversation =
                    viewModel.domainConversations.value.find { it.id == conversation.id }
                        ?: return@conversationDelegate

                viewModel.openMessagesHistoryScreen(
                    dataConversation,
                    conversation.conversationUser,
                    conversation.conversationGroup
                )
            },
            onItemLongClickListener = { conversation ->
                showOptionsDialog(conversation)
                true
            }
        )
        adapter.addDelegate(conversationsDelegate)

        binding.recyclerView.adapter = adapter
    }

    private var appBarElevationAnimator: ValueAnimator? = null

    private fun applyAppBarElevation(isLifted: Boolean) {
        val currentElevation = binding.appBar.elevation
        val elevationToSet = if (isLifted) 6.dpToPx().toFloat() else 0F

        if (!isLifted && currentElevation > 0) {
            appBarElevationAnimator?.cancel()
        }

        if (appBarElevationAnimator?.isRunning.isTrue ||
            currentElevation == elevationToSet
        ) return

        appBarElevationAnimator = ValueAnimator.ofFloat(
            currentElevation, elevationToSet
        ).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { animator ->
                val value = animator.animatedValue as Float
                binding.appBar.elevation = value
            }

            doOnEnd {
                appBarElevationAnimator = null
            }
            start()
        }
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
                    R.attr.colorPrimary
                )
            )
            setOnRefreshListener { viewModel.loadConversations() }
        }
    }

    private fun listenViewModel() {
        viewModel.uiConversations.listenValue(adapter::setItems)
    }

    private fun showOptionsDialog(uiConversations: VkConversationUi) {
        val conversationsList = viewModel.domainConversations.value.toMutableList()

        val conversationIndex =
            conversationsList.findIndex { it.id == uiConversations.conversationId } ?: return

        val conversation = conversationsList[conversationIndex]

        var canPinOneMoreDialog = true
        if (adapter.itemCount > 4) {
            if (viewModel.pinnedConversationsCount.value == 5 && conversationIndex > 4) {
                canPinOneMoreDialog = false
            }
        }

        val read = "Mark as read"

        val pin = getString(
            if (conversation.isPinned()) R.string.conversation_context_action_unpin
            else R.string.conversation_context_action_pin
        )

        val delete = getString(R.string.conversation_context_action_delete)

        val params = mutableListOf<String>()

        conversation.lastMessage?.run {
            if (!this.isRead(conversation) && !isOut) {
                params += read
            }
        }

        if (canPinOneMoreDialog) params += pin

        params += delete

        val arrayParams = params.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setItems(arrayParams) { _, which ->
                when (params[which]) {
                    read -> viewModel.readConversation(conversation)
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
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showPinConversationDialog(conversation: VkConversationDomain) {
        val isPinned = conversation.isPinned()
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
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
