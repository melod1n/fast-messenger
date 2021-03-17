package com.meloda.fast.fragment.ui.presenter

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.arrayutils.ArrayUtils
import com.meloda.fast.activity.MessagesActivityDeprecated
import com.meloda.fast.adapter.UsersAdapterDeprecated
import com.meloda.fast.fragment.ui.repository.FriendsRepositoryDeprecated
import com.meloda.fast.fragment.ui.view.FriendsViewDeprecated
import com.meloda.fast.listener.ItemClickListener
import com.meloda.fast.util.AndroidUtils
import com.meloda.mvp.MvpOnResponseListener
import com.meloda.mvp.MvpPresenter
import com.meloda.vksdk.model.VKUser

class FriendsPresenterDeprecated(viewState: FriendsViewDeprecated) :
    MvpPresenter<VKUser, FriendsRepositoryDeprecated, FriendsViewDeprecated>(
        viewState,
        FriendsRepositoryDeprecated::class.java.name
    ),
    ItemClickListener {

    companion object {
        const val ONLY_ONLINE = "_only_online"

        const val DEFAULT_FRIENDS_COUNT = 30
    }

    private var userId: Int = 0
    private var friendsCount: Int = 0

    private lateinit var adapter: UsersAdapterDeprecated

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager

    fun setup(userId: Int, recyclerView: RecyclerView, refreshLayout: SwipeRefreshLayout) {
        this.userId = userId
        this.recyclerView = recyclerView
        this.context = recyclerView.context
        this.layoutManager = recyclerView.layoutManager as LinearLayoutManager

        setRecyclerViewScrollListener(recyclerView)
        setRefreshListener(refreshLayout)

        createAdapter()

        getCachedFriends(userId, 0, DEFAULT_FRIENDS_COUNT, false, object : MvpOnResponseListener<Any?> {
            override fun onResponse(response: Any?) {
                setState(if (adapter.isEmpty()) MvpPresenter.ListState.EMPTY_LOADING else ListState.FILLED_LOADING)
                loadFriends(userId, 0, DEFAULT_FRIENDS_COUNT)
            }

            override fun onError(t: Throwable) {
                setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)
                loadFriends(userId, 0, DEFAULT_FRIENDS_COUNT)
            }
        })
    }

    private fun getCachedFriends(
        userId: Int,
        offset: Int = 0,
        count: Int = DEFAULT_FRIENDS_COUNT,
        onlyOnline: Boolean = false,
        listener: MvpOnResponseListener<Any?>? = null
    ) {
        setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)

        repository.getCachedFriends(
            userId,
            offset,
            count,
            onlyOnline,
            object : MvpOnResponseListener<ArrayList<VKUser>> {
                override fun onResponse(response: ArrayList<VKUser>) {
                    val friends = ArrayUtils.cut(response, offset, count)

                    fillAdapter(friends, offset)

                    setState(if (adapter.isEmpty()) ListState.EMPTY else ListState.FILLED)

                    listener?.onResponse(null)
                }

                override fun onError(t: Throwable) {
                    setState(if (adapter.isEmpty()) ListState.EMPTY_ERROR else ListState.FILLED)

                    listener?.onError(t)
                }
            })
    }

    private fun loadFriends(
        userId: Int,
        offset: Int = 0,
        count: Int = DEFAULT_FRIENDS_COUNT,
        onlyOnline: Boolean = false,
        listener: MvpOnResponseListener<Any?>? = null
    ) {
        if (!AndroidUtils.hasConnection()) {
            setState(if (adapter.isEmpty()) ListState.EMPTY_NO_INTERNET else ListState.FILLED)
            return
        } else {
            setState(if (adapter.isEmpty()) ListState.EMPTY_LOADING else ListState.FILLED_LOADING)
        }

        repository.loadFriends(
            userId,
            offset,
            count,
            object : MvpOnResponseListener<ArrayList<VKUser>> {
                override fun onResponse(response: ArrayList<VKUser>) {
                    friendsCount = VKUser.friendsCount

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

    private fun setRecyclerViewScrollListener(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    if (adapter.isLastItem() && !adapter.isLoading && adapter.itemCount < friendsCount) {
                        adapter.isLoading = true

                        val position = adapter.itemCount - 1
//                            adapter.itemCount - 1 - (layoutManager.findLastCompletelyVisibleItemPosition() - layoutManager.findFirstCompletelyVisibleItemPosition())

                        setState(ListState.FILLED_LOADING)
                        if (AndroidUtils.hasConnection()) {
                            loadFriends(
                                userId,
                                adapter.itemCount,
                                DEFAULT_FRIENDS_COUNT,
                                false,
                                object : MvpOnResponseListener<Any?> {
                                    override fun onResponse(response: Any?) {
                                        recyclerView.scrollToPosition(position)

                                        adapter.isLoading = false
                                    }

                                    override fun onError(t: Throwable) {
                                        viewState.showErrorSnackbar(t)
                                    }
                                })
                        } else {
                            getCachedFriends(
                                userId,
                                adapter.itemCount,
                                DEFAULT_FRIENDS_COUNT,
                                false,
                                object : MvpOnResponseListener<Any?> {
                                    override fun onResponse(response: Any?) {
                                        recyclerView.scrollToPosition(position)

                                        adapter.isLoading = false
                                    }

                                    override fun onError(t: Throwable) {
                                        viewState.showErrorSnackbar(t)
                                    }
                                })
                        }

                        Log.d("RecyclerView", "Bottom reached")
                    }
                }
            }
        })
    }

    private fun setRefreshListener(refreshLayout: SwipeRefreshLayout) {
        refreshLayout.setOnRefreshListener { loadFriends(userId) }
    }

    private fun createAdapter() {
        adapter = UsersAdapterDeprecated(context!!, arrayListOf()).also {
            it.itemClickListener = this
        }

        recyclerView.adapter = adapter
    }

    private fun fillAdapter(values: ArrayList<VKUser>, offset: Int) {
        val oldItems = ArrayList(adapter.values)

        if (offset > 0) {
            adapter.addAll(values)
        } else {
            adapter.updateValues(values)
        }

//        adapter.notifyDataSetChanged()
        adapter.notifyChanges(oldItems)

        if (offset == 0) recyclerView.scrollToPosition(0)
    }

    private fun openChat(position: Int) {
        val user = adapter[position]

        val data = Bundle().apply {
            putInt(MessagesActivityDeprecated.TAG_EXTRA_ID, user.userId)
            putString(MessagesActivityDeprecated.TAG_EXTRA_TITLE, user.toString())
            putString(MessagesActivityDeprecated.TAG_EXTRA_AVATAR, user.photo200)
        }
    }

    override fun onItemClick(position: Int) {

    }

}