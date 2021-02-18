package ru.melod1n.project.vkm.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.api.UserConfig
import ru.melod1n.project.vkm.api.VKApiKeys
import ru.melod1n.project.vkm.api.model.VKUser
import ru.melod1n.project.vkm.base.BaseActivity
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.common.FragmentSwitcher
import ru.melod1n.project.vkm.common.TaskManager
import ru.melod1n.project.vkm.common.TimeManager
import ru.melod1n.project.vkm.dialog.AccountDialog
import ru.melod1n.project.vkm.extensions.ContextExtensions.color
import ru.melod1n.project.vkm.extensions.ContextExtensions.drawable
import ru.melod1n.project.vkm.extensions.DrawableExtensions.tint
import ru.melod1n.project.vkm.fragment.FragmentConversations
import ru.melod1n.project.vkm.fragment.FragmentFriends
import ru.melod1n.project.vkm.fragment.FragmentSettings
import ru.melod1n.project.vkm.fragment.LoginFragment
import ru.melod1n.project.vkm.listener.OnResponseListener
import ru.melod1n.project.vkm.service.LongPollService
import ru.melod1n.project.vkm.util.AndroidUtils
import ru.melod1n.project.vkm.util.ViewUtils
import ru.melod1n.project.vkm.widget.Toolbar


class MainActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentConversations: FragmentConversations
    private lateinit var fragmentFriends: FragmentFriends
    private lateinit var fragmentSettings: FragmentSettings

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
        fragmentSettings = FragmentSettings()

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