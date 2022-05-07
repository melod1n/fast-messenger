package com.meloda.fast.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.viewbinding.library.activity.viewBinding
import androidx.core.view.size
import androidx.datastore.preferences.core.edit
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.AppSettings
import com.meloda.fast.common.Screens
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.dataStore
import com.meloda.fast.database.dao.AccountsDao
import com.meloda.fast.databinding.ActivityMainBinding
import com.meloda.fast.extensions.gone
import com.meloda.fast.extensions.toggleVisibility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class MainActivity : BaseActivity(R.layout.activity_main) {

    private val navigator = object : AppNavigator(this, R.id.root_fragment_container) {
        override fun setupFragmentTransaction(
            screen: FragmentScreen,
            fragmentTransaction: FragmentTransaction,
            currentFragment: Fragment?,
            nextFragment: Fragment
        ) {
        }
    }

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var updateManager: UpdateManager

    @Inject
    lateinit var accountsDao: AccountsDao

    val binding: ActivityMainBinding by viewBinding()

    var useNavDrawer: Boolean by Delegates.observable(false) { _, _, _ ->
        syncNavigationMode()
    }

    override fun onResumeFragments() {
        navigatorHolder.setNavigator(navigator)
        super.onResumeFragments()
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.navigationBar.gone()

        lifecycleScope.launch {
            dataStore.data.map { data ->
                useNavDrawer = data[AppSettings.keyUseNavigationDrawer] ?: false
            }.collect()
        }

        if (UserConfig.currentUserId == -1) {
            openMainScreen()
        } else {
            initUserConfig()
        }

        updateManager.checkUpdates { _, item ->
            if (item != null) {
                router.navigateTo(Screens.Updates())
            }
        }

        binding.drawer.getHeaderView(0).setOnLongClickListener {
            lifecycleScope.launch {
                dataStore.edit { settings ->
                    val useNavDrawer = settings[AppSettings.keyUseNavigationDrawer] ?: false
                    settings[AppSettings.keyUseNavigationDrawer] = !useNavDrawer

                    finish()
                    startActivity(Intent(this@MainActivity, MainActivity::class.java))
                }
            }
            true
        }

        syncNavigationMode()
        binding.navigationBar.selectedItemId = R.id.messages
    }

    private fun addTestMenuItem() {
        val test = binding.navigationBar.menu.add("Test")
        test.setIcon(R.drawable.ic_round_settings_24)
        test.setOnMenuItemClickListener {
            if (binding.navigationBar.menu.size < 5) {
                addClearMenuItem()
            } else {
                binding.navigationBar.menu.clear()
                addTestMenuItem()
            }

            true
        }
    }

    private fun addClearMenuItem() {
        binding.navigationBar.menu.add("Test").run {
            setIcon(R.drawable.ic_round_settings_24)
            setOnMenuItemClickListener {
                binding.navigationBar.menu.clear()
                addTestMenuItem()
                true
            }
        }
    }

    private fun initUserConfig() {
        if (UserConfig.currentUserId == -1) return

        lifecycleScope.launch {
            val accounts = accountsDao.getAll()

            Log.d("MainActivity", "initUserConfig: accounts: $accounts")
            if (accounts.isNotEmpty()) {
                val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                if (currentAccount != null) {
                    UserConfig.parse(currentAccount)
                }

                openMainScreen()
            }
        }
    }

    private fun openMainScreen() {
        router.newRootScreen(Screens.Main())
    }

    private fun syncNavigationMode() {
//        binding.navigationBar.toggleVisibility(!useNavDrawer)
        binding.drawerLayout.setDrawerLockMode(
            if (useNavDrawer) DrawerLayout.LOCK_MODE_UNLOCKED
            else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )
    }

    fun toggleNavBarVisibility(isVisible: Boolean, smooth: Boolean = false) {
        if (true) {
            binding.navigationBar.gone()
            return
        }

        if (useNavDrawer) {
            binding.navigationBar.gone()
        } else {
            if (smooth) {
                binding.navigationBar.toggleVisibility(isVisible)
            } else {
                binding.navigationBar.toggleVisibility(isVisible)
            }
        }
    }
}