package com.meloda.fast.fragment.ui.presenter

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.fast.adapter.ChatsAdapter
import com.meloda.fast.common.TimeManager
import com.meloda.fast.fragment.ui.repository.ChatsRepository
import com.meloda.fast.fragment.ui.view.ChatsView
import com.meloda.fast.listener.ItemClickListener
import com.meloda.fast.listener.ItemLongClickListener
import com.meloda.fast.util.AndroidUtils
import com.meloda.mvp.MvpOnLoadListener
import com.meloda.mvp.MvpPresenter
import com.meloda.vksdk.model.VKConversation

class ChatsPresenter(viewState: ChatsView) :
    MvpPresenter<VKConversation, ChatsRepository, ChatsView>(
        viewState, ChatsRepository::class.java.name
    ),
    ItemClickListener,
    ItemLongClickListener,
    TimeManager.OnMinuteChangeListener {

    companion object {
        const val DEFAULT_CONVERSATIONS_COUNT = 30
    }

    private lateinit var adapter: ChatsAdapter

    override fun onViewCreated(bundle: Bundle?) {
        viewState.initViews()
    }

    fun setup(recyclerView: RecyclerView, refreshLayout: SwipeRefreshLayout) {
        viewState.prepareViews()

        createAdapter()
    }

    private fun createAdapter() {
        adapter = ChatsAdapter(requireContext(), arrayListOf()).also {
            it.itemClickListener = this
            it.itemLongClickListener = this
        }

    }

    private fun fillAdapter(conversations: ArrayList<VKConversation>, offset: Int) {

    }

    private fun getCachedConversations(
        offset: Int = 0,
        count: Int = DEFAULT_CONVERSATIONS_COUNT,
        listener: MvpOnLoadListener? = null
    ) {
        listener?.onSuccess()
    }

    private fun loadConversations(
        offset: Int = 0,
        count: Int = DEFAULT_CONVERSATIONS_COUNT,
        listener: MvpOnLoadListener? = null
    ) {
        if (AndroidUtils.hasConnection()) {

        } else {

        }
    }

    override fun onItemClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemLongClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMinuteChange(currentMinute: Int) {
        TODO("Not yet implemented")
    }

}