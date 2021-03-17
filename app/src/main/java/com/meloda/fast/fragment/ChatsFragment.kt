package com.meloda.fast.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.concurrent.TaskManager
import com.meloda.fast.R
import com.meloda.fast.adapter.ConversationsAdapterDeprecated
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.database.CacheStorage
import com.meloda.fast.widget.Toolbar
import com.meloda.vksdk.OnResponseListener
import com.meloda.vksdk.VKApi
import com.meloda.vksdk.VKConstants
import com.meloda.vksdk.model.VKConversation
import com.meloda.vksdk.model.VKMessage

class ChatsFragment : BaseFragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var noItemsView: LinearLayout
    private lateinit var noInternetView: LinearLayout
    private lateinit var errorView: LinearLayout

    private lateinit var adapter: ConversationsAdapterDeprecated

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_conversations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        prepareViews()

        createAdapter()

        loadConversations()
    }

    private fun initViews() {
        toolbar = requireView().findViewById(R.id.toolbar)
        progressBar = requireView().findViewById(R.id.progressBar)
        recyclerView = requireView().findViewById(R.id.recyclerView)
        refreshLayout = requireView().findViewById(R.id.refreshLayout)

        noItemsView = requireView().findViewById(R.id.noItemsView)
        noInternetView = requireView().findViewById(R.id.noInternetView)
        errorView = requireView().findViewById(R.id.errorView)
    }

    private fun prepareViews() {
        prepareRecyclerView()
    }

    private fun prepareRecyclerView() {
        val manager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        recyclerView.layoutManager = manager
    }

    private fun createAdapter() {
        adapter = ConversationsAdapterDeprecated(recyclerView, arrayListOf())
        recyclerView.adapter = adapter
    }

    private fun loadConversations() {
        TaskManager.execute {
            VKApi.messages()
                .getConversations()
                .filter("all")
                .extended(true)
                .fields(VKConstants.USER_FIELDS)
                .offset(0)
                .count(30)
                .executeArray(
                    VKConversation::class.java,
                    object : OnResponseListener<ArrayList<VKConversation>> {
                        override fun onResponse(response: ArrayList<VKConversation>) {
                            TaskManager.execute {
                                CacheStorage.chatsStorage.insertValues(response)

                                val lastMessages = arrayListOf<VKMessage>()
                                response.forEach { lastMessages.add(it.lastMessage) }

                                CacheStorage.messagesStorage.insertValues(lastMessages)
                                CacheStorage.usersStorage.insertValues(VKConversation.profiles)
                                CacheStorage.groupsStorage.insertValues(VKConversation.groups)
                            }

                            adapter.updateValues(response)
                            adapter.notifyDataSetChanged()
                        }

                        override fun onError(t: Throwable) {

                        }
                    })
        }
    }

}