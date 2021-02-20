package com.meloda.fast.fragment

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.meloda.fast.R
import com.meloda.fast.activity.DropUserDataActivity
import com.meloda.fast.activity.UpdateActivity
import com.meloda.fast.base.BaseActivity
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.TaskManager
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.util.AndroidUtils

class FragmentSettings : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {

    companion object {

        const val CATEGORY_GENERAL = "general"
        const val KEY_HIDE_KEYBOARD_ON_SCROLL_UP = "hide_keyboard_on_scroll_up"

        const val CATEGORY_APPEARANCE = "appearance"
        const val KEY_EXTENDED_CONVERSATIONS = "appearance_extended_conversations"
        const val KEY_THEME = "appearance_theme"

        const val CATEGORY_ABOUT = "about"
        const val KEY_APP_VERSION = "app_version"

        const val CATEGORY_ACCOUNT = "account"
        const val KEY_ACCOUNT_LOGOUT = "account_logout"

        const val CATEGORY_DEBUG = "debug"
        const val KEY_CLEAR_USERS_GROUPS_CACHE = "clear_users_groups_cache"
    }

    private var currentPreferenceLayout = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)
        currentPreferenceLayout = R.xml.fragment_settings
        init()
    }

    private fun init() {
        setTitle()
        setNavigationIcon()
        setPreferencesFromResource(currentPreferenceLayout, null)

        val general = findPreference<Preference>(CATEGORY_GENERAL)
        general?.onPreferenceClickListener = rootLayoutClickListener

        val account = findPreference<Preference>(CATEGORY_ACCOUNT)
        account?.onPreferenceClickListener = rootLayoutClickListener

        val logout = findPreference<Preference>(KEY_ACCOUNT_LOGOUT)
        logout?.onPreferenceClickListener = this

        val about = findPreference<Preference>(CATEGORY_ABOUT)
        about?.onPreferenceClickListener = rootLayoutClickListener

        val appVersion = findPreference<Preference>(KEY_APP_VERSION)
        appVersion?.onPreferenceClickListener = this

        val appearance = findPreference<Preference>(CATEGORY_APPEARANCE)
        appearance?.onPreferenceClickListener = rootLayoutClickListener

        val extendedConversations = findPreference<Preference>(KEY_EXTENDED_CONVERSATIONS)
        extendedConversations?.onPreferenceChangeListener = this

        val theme = findPreference<Preference>(KEY_THEME)
        theme?.onPreferenceChangeListener = this

        val debug = findPreference<Preference>(CATEGORY_DEBUG)
        debug?.onPreferenceClickListener = rootLayoutClickListener
        updateDebugCategoryVisibility()

        val clearUsersGroupsCache = findPreference<Preference>(KEY_CLEAR_USERS_GROUPS_CACHE)
        clearUsersGroupsCache?.onPreferenceClickListener = this

        applyTintInPreferenceScreen(preferenceScreen)
    }

    override fun onResume() {
        super.onResume()

        updateDebugCategoryVisibility()
    }

    private fun updateDebugCategoryVisibility() {
        findPreference<Preference>(CATEGORY_DEBUG)?.isVisible =
            AndroidUtils.isDeveloperSettingsEnabled(requireContext())
    }

    private val rootLayoutClickListener =
        Preference.OnPreferenceClickListener { changeRootLayout(it) }

    private fun setNavigationIcon() {
        val drawable =
            if (currentPreferenceLayout == R.xml.fragment_settings) null
            else requireContext().drawable(R.drawable.ic_arrow_back)

        drawable?.setTint(requireContext().color(R.color.accent))
    }

    private fun setTitle() {
        var title = R.string.navigation_settings
        when (currentPreferenceLayout) {
            R.xml.fragment_settings_general -> title = R.string.prefs_general
            R.xml.fragment_settings_appearance -> title = R.string.prefs_appearance
            R.xml.fragment_settings_about -> title = R.string.prefs_about
            R.xml.fragment_settings_account -> title = R.string.prefs_account
        }
        requireActivity().setTitle(title)
    }

    private fun changeRootLayout(preference: Preference): Boolean {
        currentPreferenceLayout = when (preference.key) {
            CATEGORY_GENERAL -> R.xml.fragment_settings_general
            CATEGORY_ABOUT -> R.xml.fragment_settings_about
            CATEGORY_ACCOUNT -> R.xml.fragment_settings_account
            CATEGORY_APPEARANCE -> R.xml.fragment_settings_appearance
            CATEGORY_DEBUG -> R.xml.fragment_settings_debug
            else -> R.xml.fragment_settings
        }

        init()
        return true
    }

    private fun applyTintInPreferenceScreen(rootScreen: PreferenceScreen) {
        if (rootScreen.preferenceCount > 0) {
            for (i in 0 until rootScreen.preferenceCount) {
                val preference = rootScreen.getPreference(i)
                tintPreference(preference)
            }
        }
    }

    private fun tintPreference(preference: Preference) {
        if (preference.icon != null && context != null) {
            preference.icon.setTint(requireContext().color(R.color.accent))
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference.key) {
            KEY_ACCOUNT_LOGOUT -> {
                logout()
                return true
            }
            KEY_APP_VERSION -> {
                openUpdateScreen()
                return true
            }
            KEY_CLEAR_USERS_GROUPS_CACHE -> {
                showClearCacheConfirmation()
            }
        }
        return false
    }

    private fun showClearCacheConfirmation() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setMessage("Clear cache?")
        builder.setPositiveButton("Yes") { _, _ ->
            TaskManager.execute {
                AppGlobal.database.users.clear()
                AppGlobal.database.groups.clear()
            }
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun openUpdateScreen() {
        startActivity(Intent(requireContext(), UpdateActivity::class.java))
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference.key) {
            KEY_EXTENDED_CONVERSATIONS -> {
                return true
            }
            KEY_THEME -> {
                AppGlobal.instance.applyNightMode(newValue as String)
                (requireActivity() as BaseActivity).apply {
//                    applyNightMode()
                    finish()
                    startActivity(intent)
//                    recreate()
                }

                return true
            }
        }

        return false
    }

    fun onBackPressed() = if (currentPreferenceLayout == R.xml.fragment_settings) {
        true
    } else {
        currentPreferenceLayout = R.xml.fragment_settings
        init()
        false
    }

    private fun logout() {
        startActivity(Intent(requireContext(), DropUserDataActivity::class.java))
    }
}