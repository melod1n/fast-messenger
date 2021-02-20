package com.meloda.fast.fragment.ui.presenter

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.fast.BuildConfig
import com.meloda.fast.activity.MessagesActivity
import com.meloda.fast.adapter.ConversationsAdapter
import com.meloda.fast.adapter.diffutil.ConversationsCallback
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.util.VKUtil
import com.meloda.fast.common.TaskManager
import com.meloda.fast.common.TimeManager
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.fragment.ui.repository.ConversationsRepository
import com.meloda.fast.fragment.ui.view.ConversationsView
import com.meloda.fast.listener.ItemClickListener
import com.meloda.fast.listener.ItemLongClickListener
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.ArrayUtils
import com.meloda.mvp.MvpOnLoadListener
import com.meloda.mvp.MvpPresenter
import java.util.*

class ConversationsPresenter(viewState: ConversationsView) :
    MvpPresenter<VKConversation, ConversationsRepository, ConversationsView>(
        viewState,
        ConversationsRepository::class.java.name
    ),
    ItemClickListener,
    ItemLongClickListener,
    TimeManager.OnMinuteChangeListener {

    companion object {
        const val DEFAULT_CONVERSATIONS_COUNT = 30
    }

    private var conversationsCount: Int = 0

    private lateinit var adapter: ConversationsAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    fun setup(recyclerView: RecyclerView, refreshLayout: SwipeRefreshLayout) {
        this.recyclerView = recyclerView
        this.context = recyclerView.context
        this.layoutManager = recyclerView.layoutManager as LinearLayoutManager

        prepareViews()

//        setRecyclerViewScrollListener(recyclerView)
        setRefreshLayoutListener(refreshLayout)

        createAdapter()

        TimeManager.addOnMinuteChangeListener(this)

        loadConversations(0, DEFAULT_CONVERSATIONS_COUNT)

//        getCachedConversations(0, DEFAULT_CONVERSATIONS_COUNT, object : MvpOnLoadListener<Any?> {
//            override fun onResponse(response: Any?) {
//                setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)
//                loadConversations(0, DEFAULT_CONVERSATIONS_COUNT)
//            }
//
//            override fun onError(t: Throwable) {
//                setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)
//                loadConversations(0, DEFAULT_CONVERSATIONS_COUNT)
//            }
//        })
    }

    private fun setRecyclerViewScrollListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    if (adapter.isLastItem() && !adapter.isLoading && adapter.itemCount < conversationsCount) {
                        adapter.isLoading = true

                        val position = adapter.itemCount - 1
//                            adapter.itemCount - 1 - (layoutManager.findLastCompletelyVisibleItemPosition() - layoutManager.findFirstCompletelyVisibleItemPosition())

                        setState(ListState.FILLED_LOADING)
                        if (AndroidUtils.hasConnection()) {
                            loadConversations(adapter.itemCount, DEFAULT_CONVERSATIONS_COUNT,
                                object : MvpOnLoadListener<Any?> {
                                    override fun onResponse(response: Any?) {
                                        recyclerView.scrollToPosition(position)

                                        adapter.isLoading = false
                                    }

                                    override fun onError(t: Throwable) {
                                        viewState.showErrorSnackbar(t)
                                    }
                                })
                        } else {
                            getCachedConversations(adapter.itemCount, DEFAULT_CONVERSATIONS_COUNT,
                                object : MvpOnLoadListener<Any?> {
                                    override fun onResponse(response: Any?) {
                                        recyclerView.scrollToPosition(position)

                                        adapter.isLoading = false
                                    }

                                    override fun onError(t: Throwable) {
                                        viewState.showErrorSnackbar(t)
                                    }
                                })
                        }

                        if (BuildConfig.DEBUG)
                            Log.d("RecyclerView", "Bottom reached")
                    }
                }
            }
        })
    }

    private fun setRefreshLayoutListener(refreshLayout: SwipeRefreshLayout) {
        refreshLayout.setOnRefreshListener { loadConversations() }
    }

    private fun getCachedConversations(
        offset: Int = 0,
        count: Int = DEFAULT_CONVERSATIONS_COUNT,
        listener: MvpOnLoadListener<Any?>? = null
    ) {
        setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)

        repository.getCachedConversations(offset, count,
            object : MvpOnLoadListener<ArrayList<VKConversation>> {
                override fun onResponse(response: ArrayList<VKConversation>) {
                    conversationsCount = response.size

                    val conversations = ArrayUtils.cut(response, offset, count)

                    fillAdapter(conversations, offset)

                    setState(if (adapter.isEmpty()) ListState.EMPTY else ListState.FILLED)

                    listener?.onResponse(null)
                }

                override fun onError(t: Throwable) {
                    setState(if (adapter.isEmpty()) ListState.EMPTY_ERROR else ListState.FILLED)

                    listener?.onError(t)
                }
            })
    }

    private fun loadConversations(
        offset: Int = 0,
        count: Int = DEFAULT_CONVERSATIONS_COUNT,
        listener: MvpOnLoadListener<Any?>? = null
    ) {
        if (!AndroidUtils.hasConnection()) {
            setState(if (adapter.isEmpty()) ListState.EMPTY_NO_INTERNET else ListState.FILLED)
            return
        } else {
            setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)
        }

        repository.loadConversations(offset, count,
            object : MvpOnLoadListener<ArrayList<VKConversation>> {
                override fun onResponse(response: ArrayList<VKConversation>) {
                    conversationsCount = VKConversation.conversationsCount

                    fillAdapter(response, offset)

                    setState(if (adapter.isEmpty()) ListState.EMPTY else ListState.FILLED)

                    listener?.onResponse(null)
                }

                override fun onError(t: Throwable) {
                    setState(if (adapter.isEmpty()) ListState.EMPTY_ERROR else ListState.FILLED)

                    listener?.onError(t)
                }
            })
    }

    override fun destroy() {
        adapter.destroy()
        TimeManager.removeOnMinuteChangeListener(this)
    }

    private fun createAdapter() {
        adapter = ConversationsAdapter(recyclerView, arrayListOf()).also {
            it.itemClickListener = this
            it.itemLongClickListener = this
        }

        recyclerView.adapter = adapter
    }

    private fun fillAdapter(conversations: ArrayList<VKConversation>, offset: Int) {
        val oldItems = ArrayList(adapter.values)

        if (offset > 0) {
            adapter.addAll(conversations)
        } else {
            adapter.updateValues(conversations)
        }

        adapter.notifyChanges(oldItems)

        if (offset == 0) recyclerView.scrollToPosition(0)
    }

    override fun onItemClick(position: Int) {
        openChat(adapter[position])
    }

    override fun onItemLongClick(position: Int) {

    }

    override fun onMinuteChange(currentMinute: Int) {
        post { adapter.notifyItemRangeChanged(0, adapter.itemCount, ConversationsCallback.DATE) }
    }

    private fun openChat(conversation: VKConversation) {
        TaskManager.execute {
            val peerUser = MemoryCache.getUserById(conversation.conversationId)
            val peerGroup = MemoryCache.getGroupById(conversation.conversationId)

            val extras = Bundle().also {
                it.putInt(MessagesActivity.TAG_EXTRA_ID, conversation.conversationId)
                it.putString(
                    MessagesActivity.TAG_EXTRA_TITLE,
                    VKUtil.getTitle(conversation, peerUser, peerGroup)
                )
                it.putString(
                    MessagesActivity.TAG_EXTRA_AVATAR,
                    VKUtil.getAvatar(conversation, peerUser, peerGroup)
                )
                it.putSerializable(MessagesActivity.TAG_EXTRA_USER, peerUser)
                it.putSerializable(MessagesActivity.TAG_EXTRA_GROUP, peerGroup)
            }

            post { viewState.openChat(extras) }
        }

    }

}