package com.meloda.fast.fragment

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.concurrent.EventInfo
import com.meloda.concurrent.TaskManager
import com.meloda.fast.R
import com.meloda.fast.UserConfig
import com.meloda.fast.activity.MessagesActivityDeprecated
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.fragment.ui.presenter.FriendsPresenterDeprecated
import com.meloda.fast.fragment.ui.view.FriendsViewDeprecated
import com.meloda.fast.util.ViewUtils
import com.meloda.fast.widget.Toolbar
import com.meloda.vksdk.VKApiKeys

class FragmentFriendsDeprecated(private val userId: Int = 0) : BaseFragment(),
    FriendsViewDeprecated {

    private lateinit var presenterDeprecated: FriendsPresenterDeprecated

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var noItemsView: LinearLayout
    private lateinit var noInternetView: LinearLayout
    private lateinit var errorView: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()

        prepareToolbar()
        prepareRecyclerView()
        prepareRefreshLayout()

        presenterDeprecated = FriendsPresenterDeprecated(this)
        presenterDeprecated.setup(userId, recyclerView, refreshLayout)
    }

    private fun initViews() {
        toolbar = requireView().findViewById(R.id.toolbar)
        recyclerView = requireView().findViewById(R.id.recyclerView)
        refreshLayout = requireView().findViewById(R.id.refreshLayout)
        progressBar = requireView().findViewById(R.id.progressBar)

        noItemsView = requireView().findViewById(R.id.noItemsView)
        noInternetView = requireView().findViewById(R.id.noInternetView)
        errorView = requireView().findViewById(R.id.errorView)
    }

    private fun prepareToolbar() {
        initToolbar(R.id.toolbar)
        toolbar.title = getString(R.string.navigation_friends)
        setProfileAvatar()

        toolbar.inflateMenu(R.menu.fragment_friends)

        TaskManager.addOnEventListener(object : TaskManager.OnEventListener {
            override fun onNewEvent(info: EventInfo<*>) {
                if (info.key == VKApiKeys.UPDATE_USER.name) {
                    val userId = info.data as ArrayList<Int>

                    if (userId[0] == UserConfig.userId) {
                        setProfileAvatar()
                    }
                }
            }
        })
    }

    private fun setProfileAvatar() {
        TaskManager.execute {
//            AppGlobal.database.users.getById(UserConfig.userId)?.let {
//                if (it.photo100.isNotEmpty()) {
//                    runOnUi {
//                        toolbar.getAvatar().setImageURI(it.photo100)
//                    }
//                }
//            }
        }
    }

    override fun onDetach() {
        presenterDeprecated.destroy()
        super.onDetach()
    }

    private fun prepareRefreshLayout() {
        refreshLayout.setColorSchemeResources(R.color.accent)
    }

    private fun prepareRecyclerView() {
        val manager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        val decoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        decoration.setDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.divider
                )
            )
        )

        recyclerView.addItemDecoration(decoration)
        recyclerView.layoutManager = manager
    }

    override fun openChat(extras: Bundle) {
        startActivity(
            Intent(requireContext(), MessagesActivityDeprecated::class.java).putExtras(
                extras
            )
        )
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