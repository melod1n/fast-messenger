package com.meloda.fast.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meloda.fast.R
import com.meloda.fast.activity.SettingsActivityDeprecated
import com.meloda.fast.adapter.SimpleItemAdapter
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.BaseFullscreenDialog
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.extensions.DrawableExtensions.tint
import com.meloda.fast.extensions.FragmentExtensions.findViewById
import com.meloda.fast.item.SimpleMenuItem
import com.meloda.fast.listener.ItemClickListener
import com.meloda.fast.util.ColorUtils
import com.meloda.fast.util.ViewUtils
import com.meloda.fast.widget.Toolbar

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
        toolbar.tintNavigationIcon(ColorUtils.getColorAccent(requireContext()))

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
                .tint(ColorUtils.getColorAccent(requireContext())),
            requireContext().getString(R.string.navigation_settings)
        ) { openSettingsScreen() }.let { items.add(it) }

        adapter = SimpleItemAdapter(requireContext(), items).also {
            it.itemClickListener = this
        }

        recyclerView.adapter = adapter
    }

    private fun openSettingsScreen() {
        startActivity(Intent(requireContext(), SettingsActivityDeprecated::class.java))
    }

    override fun onItemClick(position: Int) {
        val item = adapter.getItem(position)

        item.clickListener?.let {
            it.onClick(requireView().findViewById(android.R.id.content))
            dismiss()
        }
    }

}