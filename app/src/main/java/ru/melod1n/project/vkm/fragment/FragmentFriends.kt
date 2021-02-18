package ru.melod1n.project.vkm.fragment

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
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.activity.MessagesActivity
import ru.melod1n.project.vkm.api.UserConfig
import ru.melod1n.project.vkm.api.VKApiKeys
import ru.melod1n.project.vkm.base.BaseFragment
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.common.TaskManager
import ru.melod1n.project.vkm.event.EventInfo
import ru.melod1n.project.vkm.extensions.FragmentExtensions.findViewById
import ru.melod1n.project.vkm.fragment.ui.presenter.FriendsPresenter
import ru.melod1n.project.vkm.fragment.ui.view.FriendsView
import ru.melod1n.project.vkm.util.ViewUtils
import ru.melod1n.project.vkm.widget.Toolbar

class FragmentFriends(private val userId: Int = 0) : BaseFragment(), FriendsView {

    private lateinit var presenter: FriendsPresenter

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

        presenter = FriendsPresenter(this)
        presenter.setup(userId, recyclerView, refreshLayout)
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
        toolbar.title = getString(R.string.navigation_friends)
        setProfileAvatar()

        toolbar.inflateMenu(R.menu.fragment_friends)

        TaskManager.addOnEventListener(object : TaskManager.OnEventListener {
            override fun onNewEvent(info: EventInfo<*>) {
                if (info.key == VKApiKeys.UPDATE_USER) {
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
            AppGlobal.database.users.getById(UserConfig.userId)?.let {
                if (it.photo100.isNotEmpty()) {
                    runOnUi {
                        toolbar.getAvatar().setImageURI(it.photo100)
                    }
                }
            }
        }
    }

    override fun onDetach() {
        presenter.destroy()
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