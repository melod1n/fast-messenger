package com.meloda.fast.fragment

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.fast.R
import com.meloda.fast.activity.MessagesActivity
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKApiKeys
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.TaskManager
import com.meloda.fast.event.EventInfo
import com.meloda.fast.extensions.FragmentExtensions.findViewById
import com.meloda.fast.extensions.FragmentExtensions.runOnUiThread
import com.meloda.fast.fragment.ui.presenter.ConversationsPresenter
import com.meloda.fast.fragment.ui.view.ConversationsView
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.ViewUtils
import com.meloda.fast.widget.Toolbar


@Suppress("UNCHECKED_CAST")
class FragmentConversations : BaseFragment(), ConversationsView {

    private lateinit var presenter: ConversationsPresenter

    private lateinit var toolbar: Toolbar
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var noItemsView: LinearLayout
    private lateinit var noInternetView: LinearLayout
    private lateinit var errorView: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_conversations, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()

        prepareToolbar()
        prepareRecyclerView()
        prepareRefreshLayout()

        presenter = ConversationsPresenter(this)
        presenter.setup(recyclerView, refreshLayout)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        refreshLayout = findViewById(R.id.refreshLayout)
        progressBar = findViewById(R.id.progressBar)

        noItemsView = findViewById(R.id.noItemsView)
        noInternetView = findViewById(R.id.noInternetView)
        errorView = findViewById(R.id.errorView)
    }

    private fun prepareToolbar() {
        initToolbar(R.id.toolbar)
        toolbar.title = getString(R.string.navigation_conversations)
        setProfileAvatar()

        TaskManager.addOnEventListener(object : TaskManager.OnEventListener {
            override fun onNewEvent(info: EventInfo<*>) {
                if (info.key == VKApiKeys.UPDATE_USER) {
                    val userIds = info.data as ArrayList<Int>

                    if (userIds.contains(UserConfig.userId)) {
                        setProfileAvatar()
                    }
                }
            }
        })
    }

    private fun prepareRefreshLayout() {
        refreshLayout.setColorSchemeResources(R.color.accent)
    }

    private fun prepareRecyclerView() {
        val manager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        decoration.setDrawable(
            ColorDrawable(AndroidUtils.getThemeAttrColor(requireContext(), R.attr.dividerHorizontal))
        )

        recyclerView.itemAnimator = null
        recyclerView.addItemDecoration(decoration)

        recyclerView.layoutManager = manager
    }

    private fun setProfileAvatar() {
        TaskManager.execute {
            AppGlobal.database.users.getById(UserConfig.userId)?.let {
                if (it.photo100.isNotEmpty()) {
                    runOnUiThread {
                        toolbar.getAvatar().setImageURI(it.photo100)
                    }
                }
            }
        }
    }

    override fun openChat(extras: Bundle) {
        startActivity(Intent(requireContext(), MessagesActivity::class.java).putExtras(extras))
    }

    override fun showErrorSnackbar(t: Throwable) {
        ViewUtils.showErrorSnackbar(requireView(), t)
    }

    override fun prepareNoItemsView() {
    }

    override fun showNoItemsView() {
        noItemsView.isVisible = true
    }

    override fun hideNoItemsView() {
        noItemsView.isVisible = false
    }

    override fun prepareNoInternetView() {
    }

    override fun showNoInternetView() {
        noInternetView.isVisible = true
    }

    override fun hideNoInternetView() {
        noInternetView.isVisible = false
    }

    override fun prepareErrorView() {

    }

    override fun showErrorView() {
        errorView.isVisible = true
    }

    override fun hideErrorView() {
        errorView.isVisible = false
    }

    override fun showProgressBar() {
        progressBar.isVisible = true
    }

    override fun hideProgressBar() {
        progressBar.isVisible = false
    }

    override fun showRefreshLayout() {
        refreshLayout.isRefreshing = true
    }

    override fun hideRefreshLayout() {
        refreshLayout.isRefreshing = false
    }
}