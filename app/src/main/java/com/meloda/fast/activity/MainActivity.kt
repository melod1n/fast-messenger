package com.meloda.fast.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKApiKeys
import com.meloda.fast.api.model.VKUser
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.FragmentSwitcher
import com.meloda.fast.common.TaskManager
import com.meloda.fast.common.TimeManager
import com.meloda.fast.dialog.AccountDialog
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.extensions.DrawableExtensions.tint
import com.meloda.fast.fragment.FragmentConversations
import com.meloda.fast.fragment.FragmentFriends
import com.meloda.fast.fragment.SettingsFragment
import com.meloda.fast.fragment.LoginFragment
import com.meloda.fast.listener.OnResponseListener
import com.meloda.fast.service.LongPollService
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.util.ViewUtils
import com.meloda.fast.widget.Toolbar


class MainActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentConversations: FragmentConversations
    private lateinit var fragmentFriends: FragmentFriends
    private lateinit var settingsFragment: SettingsFragment

    private var selectedId = 0

    private lateinit var drawerLayout: DrawerLayout
    lateinit var bottomBar: BottomNavigationView
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

//        checkLogin()

        if (UserConfig.isLoggedIn()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, FragmentConversations())
                .commit()
        } else {
            bottomBar.isVisible = false

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, LoginFragment())
                .commit()
        }


//        TimeManager.init(this)

//        prepareFragments()

//        prepareNavigationView()
//        prepareBottomBar()
//        checkLogin()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        bottomBar = findViewById(R.id.bottomBar)
        navigationView = findViewById(R.id.navigationView)
    }

    override fun onDestroy() {
        TimeManager.destroy()
        super.onDestroy()
    }

    private fun prepareFragments() {
        fragmentConversations = FragmentConversations()
        fragmentFriends = FragmentFriends(UserConfig.userId)
        settingsFragment = SettingsFragment()

        val containerId = R.id.fragmentContainer

        FragmentSwitcher.addFragments(
            supportFragmentManager,
            containerId,
            listOf(fragmentConversations)
        )
    }

    fun initToolbar(toolbar: Toolbar) {
        toolbar.navigationIcon =
            drawable(R.drawable.ic_search).tint(color(R.color.text_secondary_60_alpha))

        toolbar.setTitleMode(Toolbar.TitleMode.HINT)
        toolbar.setTitle(R.string.action_search)
        toolbar.setAvatarClickListener { openAccountDialog() }
    }

    private fun openAccountDialog() {
        AccountDialog().show(supportFragmentManager, AccountDialog.TAG)
    }

    private fun prepareNavigationView() {
        navigationView.layoutParams?.width = AppGlobal.screenWidth - AppGlobal.screenWidth / 6

        navigationView.setNavigationItemSelectedListener(this)

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun prepareBottomBar() {
//        val menu = bottomBar.menu
//
//        val navigationFriends = menu.add(R.string.navigation_friends)
//        navigationFriends.icon = drawable(R.drawable.ic_people_outline)
//
//        val navigationConversations = menu.add(R.string.navigation_conversations)
//        navigationConversations.icon = drawable(R.drawable.ic_message_outline)
//
//        val navigationImportant = menu.add(R.string.navigation_important)
//        navigationImportant.icon = drawable(R.drawable.ic_star_border)

        bottomBar.setOnNavigationItemSelectedListener(this)
    }

    private fun createMenuItem(menu: Menu, tag: String): MenuItem {
        return when (tag) {
            "friends" ->
                menu.add("Friends").apply { icon = drawable(R.drawable.ic_people_outline) }
            "conversations" ->
                menu.add("Conversations").apply { icon = drawable(R.drawable.ic_message_outline) }
            "important" ->
                menu.add("Important").apply { icon = drawable(R.drawable.ic_star_border) }

            else -> menu.add("")
        }
    }

    private fun checkLogin() {
        if (UserConfig.isLoggedIn()) {
            startLongPoll()
            loadProfileInfo()
        } else {
            openStartScreen()
        }
    }

    private fun openMainScreen() {
        selectedId = R.id.navigationConversations
        bottomBar.selectedItemId = selectedId
        openConversationsScreen()
    }

    private fun startLongPoll() {
        startService(Intent(this, LongPollService::class.java))
    }

    private fun openStartScreen() {
        finish()
        startActivity(Intent(this, StartActivity::class.java))
    }

    private fun openConversationsScreen() {
        FragmentSwitcher.showFragment(
            supportFragmentManager,
            fragmentConversations.javaClass.simpleName,
            true
        )
    }

    private fun openFriendsScreen() {
        FragmentSwitcher.showFragment(
            supportFragmentManager,
            fragmentFriends.javaClass.simpleName,
            true
        )
    }

    private fun openSettingsScreen() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun loadProfileInfo() {
        if (AndroidUtils.hasConnection()) {
            TaskManager.loadUser(VKApiKeys.UPDATE_USER, UserConfig.userId,
                object : OnResponseListener<VKUser> {
                    override fun onResponse(response: VKUser) {
                        prepareNavigationHeader(response)
                        openMainScreen()
                    }

                    override fun onError(t: Throwable) {
                        openMainScreen()
                    }
                })
        }
    }

    private fun prepareNavigationHeader(user: VKUser) {
        ViewUtils.prepareNavigationHeader(navigationView.getHeaderView(0), user)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        switchFragment(item.itemId)
        return true
    }

    private fun switchFragment(itemId: Int) {
        var valid = true

        when (itemId) {
            R.id.navigationConversations -> {
                openConversationsScreen()
            }
            R.id.navigationFriends -> {
                openFriendsScreen()
            }
            R.id.navigationSettings -> {
                openSettingsScreen()
            }
            else -> {
                valid = false
            }
        }

        if (!valid) return

        if (selectedId != itemId) {
            selectedId = itemId
            navigationView.setCheckedItem(selectedId)
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawer(navigationView)
        } else {
            super.onBackPressed()
        }
    }
}