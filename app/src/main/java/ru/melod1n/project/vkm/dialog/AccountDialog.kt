package ru.melod1n.project.vkm.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.activity.SettingsActivity
import ru.melod1n.project.vkm.adapter.SimpleItemAdapter
import ru.melod1n.project.vkm.api.UserConfig
import ru.melod1n.project.vkm.base.BaseFullscreenDialog
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.database.MemoryCache
import ru.melod1n.project.vkm.extensions.ContextExtensions.color
import ru.melod1n.project.vkm.extensions.ContextExtensions.drawable
import ru.melod1n.project.vkm.extensions.DrawableExtensions.tint
import ru.melod1n.project.vkm.extensions.FragmentExtensions.findViewById
import ru.melod1n.project.vkm.item.SimpleMenuItem
import ru.melod1n.project.vkm.listener.ItemClickListener
import ru.melod1n.project.vkm.util.ViewUtils
import ru.melod1n.project.vkm.widget.Toolbar

class AccountDialog : BaseFullscreenDialog(), ItemClickListener {

    companion object {
        const val TAG = "account_fullscreen_dialog"
    }

    private lateinit var adapter: SimpleItemAdapter

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var headerRoot: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()
        prepareToolbar()
        prepareRecyclerView()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        refreshLayout = findViewById(R.id.refreshLayout)
        headerRoot = findViewById(R.id.headerRoot)
    }

    private fun prepareToolbar() {
        toolbar.navigationIcon = requireContext().drawable(R.drawable.ic_close)
            .tint(requireContext().color(R.color.accent))


        toolbar.setTitle(R.string.account_dialog_title)
        toolbar.setTitleMode(Toolbar.TitleMode.SIMPLE)
        toolbar.setNavigationClickListener { dismiss() }

        MemoryCache.getUserById(UserConfig.userId)?.let {
            AppGlobal.handler.post { ViewUtils.prepareNavigationHeader(headerRoot, it) }
        }
    }

    private fun prepareRecyclerView() {
        refreshLayout.isEnabled = false

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        recyclerView.setHasFixedSize(true)

        createItemsAndAdapter()
    }

    private fun createItemsAndAdapter() {
        val items = arrayListOf<SimpleMenuItem>()

        SimpleMenuItem(
            requireContext().drawable(R.drawable.ic_settings_outline)
                .tint(requireContext().color(R.color.accent)),
            requireContext().getString(R.string.navigation_settings)
        ) { openSettingsScreen() }.let { items.add(it) }

        adapter = SimpleItemAdapter(requireContext(), items).also {
            it.itemClickListener = this
        }

        recyclerView.adapter = adapter
    }

    private fun openSettingsScreen() {
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    override fun onItemClick(position: Int) {
        val item = adapter.getItem(position)

        item.clickListener?.let {
            it.onClick(requireView().findViewById(android.R.id.content))
            dismiss()
        }
    }

}