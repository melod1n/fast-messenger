package com.meloda.fast.screens.conversations

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.meloda.fast.R
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.base.adapter.AsyncDiffItemAdapter
import com.meloda.fast.databinding.FragmentConversationsBinding
import com.meloda.fast.ext.asUiText
import com.meloda.fast.ext.color
import com.meloda.fast.ext.dpToPx
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.showDialog
import com.meloda.fast.ext.string
import com.meloda.fast.ext.tintMenuItemIcons
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.conversations.adapter.conversationDelegate
import com.meloda.fast.util.AndroidUtils
import dev.chrisbanes.insetter.applyInsetter
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationsFragment : BaseFragment(R.layout.fragment_conversations) {

    private val viewModel: ConversationsViewModel by viewModel<ConversationsViewModelImpl>()

    private val binding by viewBinding(FragmentConversationsBinding::bind)
    private val adapter by lazy { AsyncDiffItemAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareView()
        listenViewModel()
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
        binding.toolbar.setOnMenuItemClickListener { item ->
            viewModel.onToolbarMenuItemClicked(item.itemId)
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
            onItemClickListener = viewModel::onConversationItemClick,
            onItemLongClickListener = viewModel::onConversationItemLongClick
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
                if (view == null) return@addUpdateListener
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
            setOnRefreshListener(viewModel::onRefresh)
        }
    }

    private fun listenViewModel() = with(viewModel) {
        conversationsList.listenValue(adapter::setItems)
        isLoading.listenValue(::handleIsLoading)
        isNeedToShowOptionsDialog.listenValue(::showOptionsDialog)
        isNeedToShowDeleteDialog.listenValue(::showDeleteConversationDialog)
        isNeedToShowPinDialog.listenValue(::showPinConversationDialog)
    }

    private fun handleIsLoading(isLoading: Boolean) {
        binding.progressBar.toggleVisibility(isLoading && adapter.isEmpty())
        binding.refreshLayout.isRefreshing = isLoading && adapter.isNotEmpty()
    }

    // TODO: 06.04.2023, Danil Nikolaev: extract creating options to VM
    private fun showOptionsDialog(conversation: VkConversationDomain?) {
        if (conversation == null) return

        var canPinOneMoreDialog = true
        if (adapter.itemCount > 4) {
            if (viewModel.pinnedConversationsCount.value == 5 && !conversation.isPinned()) {
                canPinOneMoreDialog = false
            }
        }

        val read = "Mark as read"

        val pin = string(
            if (conversation.isPinned()) R.string.conversation_context_action_unpin
            else R.string.conversation_context_action_pin
        )

        val delete = string(R.string.conversation_context_action_delete)

        val params = mutableListOf<Pair<String, String>>()

        conversation.lastMessage?.run {
            if (!this.isRead(conversation) && !isOut) {
                params += "read" to read
            }
        }

        if (canPinOneMoreDialog) params += "pin" to pin

        params += "delete" to delete

        context?.showDialog(
            items = params.map { param -> param.second.asUiText() },
            itemsClickAction = { index, _ ->
                val key = params[index].first
                viewModel.onOptionsDialogOptionClicked(conversation, key)
            },
            onDismissAction = viewModel::onOptionsDialogDismissed
        )
    }

    private fun showDeleteConversationDialog(conversationId: Int?) {
        if (conversationId == null) return

        context?.showDialog(
            title = UiText.Resource(R.string.confirm_delete_conversation),
            positiveText = UiText.Resource(R.string.action_delete),
            positiveAction = { viewModel.onDeleteDialogPositiveClick(conversationId) },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onDeleteDialogDismissed
        )
    }

    private fun showPinConversationDialog(conversation: VkConversationDomain?) {
        if (conversation == null) return

        context?.showDialog(
            title = UiText.Resource(
                if (conversation.isPinned()) R.string.confirm_unpin_conversation
                else R.string.confirm_pin_conversation
            ),
            positiveText = UiText.Resource(
                if (conversation.isPinned()) R.string.action_unpin
                else R.string.action_pin
            ),
            positiveAction = {
                viewModel.onPinDialogPositiveClick(conversation)
            },
            negativeText = UiText.Resource(R.string.cancel),
            onDismissAction = viewModel::onPinDialogDismissed
        )
    }
}
